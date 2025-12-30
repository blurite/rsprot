# RSProt

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]
[![OldSchool - 221 - 235 (Alpha)](https://img.shields.io/badge/OldSchool-221--235_(Alpha)-9a1abd)](https://github.com/blurite/rsprot/tree/master/protocol/osrs-235/osrs-235-api/src/main/kotlin/net/rsprot/protocol/api)

## Status
> [!NOTE]
> This library is currently in an alpha release stage. Breaking changes may be
> done in the future.

## Alpha Usage

The artifacts can be found on [Maven Central](https://central.sonatype.com/search?q=rsprot).

In order to add it to your server, add the below line under dependencies
in your build.gradle.kts.

```kts
implementation("net.rsprot:osrs-235-api:1.0.0-ALPHA-20251224")
```

An in-depth tutorial on how to implement it will be added into this read-me
document in the future!

## Introduction
RSProt is an all-in-one networking library for private servers,
primarily targeting the OldSchool RuneScape scene. Contributions for
other revisions are welcome, but will not be provided by default.

## Prerequisites
- Kotlin 1.9.23
- Java 11

## Supported Versions
This library currently supports revision 221-235 OldSchool desktop clients.

## Quick Guide
This section covers a quick guide for how to use the protocol after implementing
the base API. It is not a guide for the base API itself, that will come in the
future. This specific quick guide refers to revision 235. Revisions older
than 235 have a significantly different API and will not be explored here.
It is recommented you upgrade to latest, or view an older readme in history.

### Info Packets
Info packets can be allocated by calling `networkService.infoProtocols.alloc(playerIndex, clientType)`.
This returns an all-in-one class that allows the server to communicate general
updates, such as build area and player coordinate changes over to all three in one.

#### Updating Avatars
Near the end of a game cycle, after all the logic processing has finalized,
you'll want to write any extended infos, movement and other changes for a
given avatar over. It is highly recommended you do this in a special pass
at the end of a game cycle, instead of midway through the game processing.
Latter can result in problems with time-delayed tasks holding a reference
to an avatar and causing changes to it after it has been deallocated and
reallocated by someone else.

#### Updating Infos
At the end of a game cycle, you'll want to invoke `infos.updateRootCoord(level, x, z)`.
Additionally, on login, reconnect, and whenever the root world map updates (via RebuildLogin, RebuildNormal or RebuildRegion),
you'll want to invoke `infos.updateRootBuildAreaCenteredOnPlayer(playerX, playerZ)`. There are alternative
methods available too, however the centered method does all the math for you, as well as any
boundary checks.

Once you've prepared all the info packets through the above step, it is time to
actually build all the packets. In order to do so, simply call
`networkservice.infoProtocols.update()`. This will run an expensive task
of building world entity infos, player infos and npc infos for each player.
This step should not ever throw any errors. Errors during processing will be
propagated to the player from the player's perspective, as it tries to request
each packet.

Once the protocols are ready, it is time to actually write it out to the player's
session.

Below is an example of how to use the RSProt-provided class of packets and
update all the infos and worlds.

```kotlin
val infoPackets = infos.getPackets()
val rootPackets = infoPackets.rootWorldInfoPackets
// First start off by updating the root world.
packets.send(rootPackets.activeWorld)
packets.send(rootPackets.npcUpdateOrigin)

rootPackets.worldEntityInfo
    .onSuccess(packets::send)
    .onFailure(::onUpdateException)

rootPackets.playerInfo
    .onSuccess(packets::send)
    .onFailure(::onUpdateException)

// OSRS seems to omit sending npc info packets if there are 0 readable
// bytes in it. It is important to however invoke safeRelease() on the packet
// if you do not submit it into Session, as it will otherwise leak.
val rootNpcInfoEmpty = rootPackets.npcInfo.isEmpty()
if (!rootNpcInfoEmpty) {
    rootPackets.npcInfo
        .onSuccess(packets::send)
        .onFailure(::onUpdateException)
} else {
	rootPackets.npcInfo.safeReleaseOrThrow()
}

// At this stage, you should submit all the zone packets from your server.
// The active world is already set to the root world, so it is just a matter
// of sending packets like UpdateZoneFullFollows, UpdateZonePartialEnclosed
// and so on.
buildAreaManager.sendRootZoneUpdates(rootPackets.activeLevel)

// When a world entity is removed from high resolution, stop tracking it
// for zone updates. If it is re-added, a full zone synchronization should
// take place.
infoPackets.removedWorldIndices.forEach(buildAreaManager::destroyWorld)

// Now go over every world entity that is still in high resolution
for (worldInfoPackets in infoPackets.activeWorlds) {
    packets.send(worldInfoPackets.activeWorld)

	// If the world entity is newly added in this cycle, make sure to send the
	// RebuildWorldEntityV2 packet for this world, to actually build the
	// map behind the world entity.
    if (worldInfoPackets.added) {
        rebuildWorldEntity(worldInfoPackets.worldId)
    }

	// As with root world, omit sending the info if it is empty,
	// and instead release safely.
    val worldNpcInfoEmpty = worldInfoPackets.npcInfo.isEmpty()
    if (!worldNpcInfoEmpty) {
		packets.send(worldInfoPackets.npcUpdateOrigin)
        worldInfoPackets.npcInfo
            .onSuccess(packets::send)
            .onFailure(::onUpdateException)
    } else {
        worldInfoPackets.npcInfo.safeReleaseOrThrow()
    }

	// Update the zones for the world entity. As before, the active world
	// and level are already assigned. You just have to send the zone packets
	// for that world, on the specified level.
    buildAreaManager.sendDynamicZoneUpdates(worldInfoPackets.worldId, worldInfoPackets.activeLevel)
}

// Lastly, set the active world back to the root world. This is important
// for some client functionality, such as client-sided pathfinding.
packets.send(rootPackets.activeWorld)
```

### NPCs
For each NPC that spawns into the world, an avatar must be allocated.
This can be done via:
```kotlin
val avatar = networkService.npcAvatarFactory.alloc(
    index,
    id,
    coord.level,
    coord.x,
    coord.z,
    spawnClock,
    direction,
)
```
Changes to the NPC, such as animations will be performed via this avatar,
using the extended info block. For movement, the respective movement functions
must be called on the avatar.
When a NPC fully despawns from the world, the avatars must be released via
`networkService.npcAvatarFactory.release(avatar)`.
If a NPC dies but is set to respawn, instead just mark the avatar inaccessible.

### World Entities
For each world entity that spawns into the world, an avatar must be allocated.
This can be done via:
```kotlin
val avatar = networkService.worldEntityAvatarFactory.alloc(
	index,
	id,
	ownerIndex,
	sizeX,
	sizeZ,
	southWestZoneX,
	southWestZoneZ,
	minLevel,
	maxLevel,
	fineX,
	fineZ,
	projectedLevel,
	activeLevel,
	angle,
)
```

Movement and turning will be controlled via this avatar.
When the world entity despawns, the avatar must be returned back into the
protocol. This can be done via:
`networkService.worldEntityAvatarFactory.release(avatar)`

#### Properties Breakdown
- index: the index of the world entity, a value from 1 to 4094.
- id: the config/type if of the world entity.
- ownerIndex: the index of the character that owns the entity. If the index is > 0, the index is assumed
  to be that of a player. If the index is < 0, the world entity is treated as "owned by an NPC".
  The index determines the priority of the world entity for rendering. The world entity on which a player
  resides will receive the highest priority. Next up is the world that matches the player's own index.
  After this comes worlds owned by NPCs, such as those used in Barracuda Trials. Lastly, worlds owned
  by other players.
- sizeX: the x size of the world entity in zones.
- sizeZ: the z size of the world entity in zones.
- southWestZoneX: the absolute south-western zone x coordinate where the world entity
truly is in the typically-instance map.
- southWestZoneZ: the absolute south-western zone z coordinate where the world entity
  truly is in the typically-instance map.
- minLevel: the minimum level for the world entity. If a player is within the world
entity's instance/land, but their level is below this, they will not be considered
as part of it, and will instead just appear as part of the root world.
- maxLevel: the maximum level for the world entity. If a player is within the world
  entity's instance/land, but their level is above this, they will not be considered
  as part of it, and will instead just appear as part of the root world.
- fineX: the fine x pivot coordinate of the world entity in the root world.
  A simple way to go from a coordgrid to a center coordfine is by calculating it
  as `absoluteX * 128 + 64`, as each coordfine is equal to 128 coordgrids.
- fineZ: the fine z pivot coordinate of the world entity in the root world.
  A simple way to go from a coordgrid to a center coordfine is by calculating it
  as `absoluteX * 128 + 64`, as each coordfine is equal to 128 coordgrids.
- projectedLevel: the level at which the world entity will render in the root world.
  This value should usually be 0, as most world entities end up in the root world.
- activeLevel: the main level on the world entity in which activity takes place.
  This variable can be accessed through the world entity's config/type,
  and it should match up with that as client will assume it from its own perspective.
- angle: the angle of the world entity. A value of 0-2047 is expected. A value
  of 0 faces south, 512 west, 1024 north and 1536 east. Interpolate for ones
  inbetween.


## API Analysis

All our Netty handlers disable auto-read by default. This means the protocol
is responsible for triggering reads whenever it is ready to continue, ensuring
we do not get an avalanche of requests from the client right away.
All handlers additionally have an idle state tracker to disconnect any clients
which have been idle for 30 seconds on JS5 and 60 seconds on other services.
On-top of this, we respect the channel writability status and avoid writing
to the clients if the writability starts returning false, ensuring we do not
grow the outbound buffer indefinitely, resulting in a potential attack vector.

###  JS5
This section discusses the JS5 implementation found for the OldSchool
variant. Other versions of the game may require different logic.

The API offers a JS5 service that runs on a single thread in order to serve
all connected clients _fairly_. The JS5 service thread will be in a waiting
stage whenever there are no clients to serve to, and gets woken up when
new requests come in. Unlike most JS5 implementation seen, this will serve
a specific block of bytes to each client, with an emphasis on the clients
logged into the game, rather than a full group or multiple groups.
This ensures that each connected client receives its fair share of data,
and that no clients get starved due to a malicious actor flooding with
requests for expensive groups. Furthermore, the service is set to flush
only when the need arises, rather than after each write into the channel.

By default, the JS5 service is set to write a 512 byte block to each
client per iteration. If the client is in a logged in state, a block
that's three times larger is written instead, so 1,536 bytes get written
to those. In both scenarios, if the remaining number of bytes
is less than that of the block length, only the remaining bytes are written,
meaning it is possible for the service to write less than the pre-defined
block size. After 10,240 bytes have been written into the channel since
the last flush, or if 10 complete requests have been written, the channel
is flushed and the trackers are reset. Additionally, if we run out of
requests to fulfill, the channel is flushed, even if the aforementioned
thresholds are not met. Every number mentioned here is configurable by the
server, should the need arise.

Two available methods of feeding data to the service are offered,
one that is based on normal Netty ByteBuf objects, and one that is based
on RandomAccessFiles, utilizing Netty's FileRegions to do zero-copy
writes. While the latter may sound enticing, it is replacing memory with
disk IO, which is likely significantly slower. As a result of that,
File Region based implementation is only recommended for development
environments, allowing one to not have to load JS5 up at all, not even
opening the cache. This of course requires the cache to have been split
into JS5-compatible files on the disk.

The underlying implementation utilizes a primitive Int-based ArrayDeque
to keep track of requests made by the client, with an upper limit of 200
requests of both the urgent and prefetch types. This ensures no garbage
is produced when adding and removing entries from the queue. Furthermore,
because the service itself is single-threaded, we can get away with using
non-thread-safe implementations which are far simpler and require less
processing power. Each connected client is kept in a unique queue that
is based on a mix of HashSet and ArrayDeque to ensure uniqueness in the
queue.

The clients are only written to as long as they can be written to, avoiding
exploding the outgoing buffer - any client which still has pending requests,
but cannot be written to, will be put in an idle state which is resumed as
soon as Netty notifies the client that the channel is once again writable.
Additionally, the client will stop reading any new requests if the number
of requests reaches 200 in either of the queues.
This JS5 implementation __does__ support encryption keys (xor).
While the feature itself is only used in abnormal circumstances, the support
for it is still offered.

### Login
This section discusses the login implementation found for the OldSchool
variant. Other versions of the game may require different logic.

By default, all game logins will require proof of work to have been completed.
The login block itself will be decoded only after proof of work has been
completed and verified correct. In any other circumstance, the connection
is rejected and the login block buffer gets released. For reconnections,
proof of work is not used - this is because reconnections are cheap to validate,
and the time taken to execute proof of work in the client may result in
the reconnection timing out.
The network service offers a way to decode login block on another thread,
away from Netty's own worker threads. With the default implementation,
a ForkJoinPool is used to execute the job. This is primarily because
RSA deciphering takes roughly half a millisecond for a 1024-bit key, creating
a potential attack vector via logins.

#### Proof of Work

The OldSchool client supports a proof of work implementation for logins,
allowing the server to throttle the client by giving it a CPU-intensive
job to execute before the login may take place. As of writing this section,
the OldSchool client only supports a SHA-256 hashing based proof of work
implementation. When the server receives the initial login request, it
has to decide whether to request proof of work from the client, or allow
the login right away. In our default implementation, proof of work is
always required for non-reconnect logins. The server sends out a request
to the client containing a large 495-byte random block of bytes turned into
a hexadecimal string. Along with the block, a difficulty value is provided.
The client will then iterate from 0 to infinity, taking the iteration number
and appending it to the end of the random text block. It then runs the
SHA-256 hashing algorithm on that string. Once done, the client has to check
if the number of leading zero bits in the resulting string is equal to or
greater than the input difficulty - if it is, it transmits that number
to the server which will validate it by running the same hashing algorithm
just once, if it isn't, the client continues looping. With a default difficulty
of 18, the client is typically does anywhere from 100,000 to 500,000 hashes
before finding a solution that works. As mentioned, the server only has to
run the hashing once to confirm the results. As the data is completely random,
the number of iterations the client has to do is rather volatile.
Some requests may finish very quickly despite a high difficulty, while others
take a lot longer with a smaller one. As such, servers should account for
bad luck and slower devices, both of which increase the likelihood of the
proof of work process taking longer than the timeout duration for logins.
Servers may also choose to implement a difficulty system that scales depending
on the number of requests over a given timespan, or any other variation of this.
The default implementation uses a static difficulty of 18.
Furthermore, it's important to note that each difficulty increase of 1
doubles the amount of work the client has to perform on average, e.g. a
difficulty of 19 is twice as difficult to figure out than a difficulty of 18.

#### Beta Worlds
As of revision 220, the OldSchool client has a semi-broken implementation
for beta worlds. Jagex had intended to add a locking mechanism to the client,
to avoid sending certain groups in the cache to anyone that hasn't successfully
logged into the beta world. While the implementation itself is relatively fine,
it does nothing to prevent someone using a headless client to request the
groups anyway. The JS5 server is completely unaware of whether the client
has passed the authorization in OldSchool, so any headless client is free
to request the entire cache whenever they want. This implementation only
prevents transmitting the cache to anyone that is trying to download it via
the default client.

Because the implementation is relatively broken and doesn't actually protect
anything, we have made a choice to not try to entertain this mechanism,
and instead the login service performs an immediate request under the hood
to request all the remaining cache CRCs before informing the server of
the login request having been made. This allows us to still pass a complete
login block that looks the same as during a non-beta-world login.
The server must enable the beta world flag in both the library and the
client itself. If the flag status does not match between the two, either an
exception is thrown during login decoding, or the login will hang due to a
lack of bytes having been transmitted by the client.

### Game Connection
The game connection implementation utilizes a flipping mechanism for auto-read
and single-decode. When the handler switches from login to game, auto-read
is enabled and single decode is disabled. As the decoder decodes more messages,
a tracker keeps track of how many user-type and client-type packets have been
received within this current cycle. If the threshold of 10 user-type packets,
or 50 client-type packets is reached, auto-read is disabled and single-decode
is enabled. This guarantees that Netty will __immediately__ halt decoding of
any more packets, even if the ctx has more bytes to be read.
At the start of each game cycle, the server is responsible for polling the
packets of each connected client. After it does so, the auto-read status
is re-enabled, and single-decode is disabled, allowing the client to continue
reading packets. Using this implementation, as our socket receive buffer
size is 65536 bytes, the maximum number of incoming bytes per channel is
only 65536 * 2 in a theoretical scenario - with the socket being full,
as well as one socket's worth of data having been transferred to Netty's
internal buffer.

As the server is responsible for providing a repository of message consumers
for game packets, we can furthermore utilize this to avoid decoding packets
for which a consumer has not been registered. In such cases, we simply
skip the number of bytes that is the given packet's size, rather than
slicing the buffer and decoding a new data class out of it.
Any packets for which there has not been registered a consumer will not
count towards message count thresholds, as the service is not aware of
the category in which the given packet belongs.

## Binary Files
As of revision 235 (support for older revisions will be added in the future),
it is possible to export RSProx-compatible .bin files.

This can be achieved in two steps:
1. Supply a BinaryHeaderProvider in your AbstractNetworkServiceFactory implementation:
As an example:
```kt
    override fun getBinaryHeaderProvider(): BinaryHeaderProvider {
        // If the provider returns null, no binary file will be produced
        // for this user.
        return BinaryHeaderProvider { index, timestamp, accountHash ->
            // You can prefix it with the player's name if you'd like
            val playerName =
                World.players[index]
                    ?.name
                    ?.lowercase()
                    ?.replace(' ', '_') ?: "unknown_player"
            val fileNameSuffix = BinaryHeaderProvider.fileName(timestamp, accountHash)
            val fileName = "$playerName-$fileNameSuffix"
            // Note: Make sure the folders for the path exist! It will error otherwise.
            PartialBinaryHeader(
                path = Path("binary", fileName),
                worldId = 1,
                worldFlags = 0x1,
                worldLocation = 0,
                worldHost = "localhost",
                worldActivity = "Development",
                clientName = "RSProt-MyServer",
            )
        }
    }
```
2. Writing the files to disk:
```kt
// Define this as static property
private val writer = BinaryBlobAppendWriter(retryCount = 2)

// Note: writer#write _can_ throw an exception and should safely be handled.
val blob = player.session.getBinaryBlobOrNull()
if (blob != null) {
    val success = writer.write(blob)
    if (!success) {
        // Make sure to try again in the future if this is supposed to be
        // the final write call. This method will return false if there's
        // a temporary in-memory lock on place, which is necessary for packet
        // groups.
        retryNextTick()
    }
}
```

There are two out-of-the-box implementations of BinaryWriter - a non-atomic
BinaryBlobAppendWriter, and an atomic BinaryBlobAtomicReplacementWriter.

The append writer will continuously append to the same file, keeping only a
small buffer in memory and resetting it with each successful invocation.
If users flush it every ~5kb
(you can check it via `player.session.getBinaryBlobOrNull()?.readableBytes()`),
there should never be more than ~10MB of heap memory allocated for this.
The downside of this implementation is that it is not atomic. While you should
be safe from multiple threads writing onto the same file simultaneously,
which is illogical given the design, you will encounter data loss and corruption
on power outages. The upside, however, is that it will only corrupt the ending,
not the stream that came prior. The non-corrupted parts can still be safely
transcribed in RSProx.

The atomic replacement writer will always write the full file into a temporary
file, and then perform an atomic move to the real path. This guarantees that
no aspect of the file will ever corrupt, but it comes at a steep cost: the buffer
is never freed in memory while that player is online, which can grow as high as
~10MB for a single player. With 2,000 players, the number can theoretically grow
as high as 20GB.

The recommended writer is the appending variant, as the lack of atomicity is
relatively minor given the circumstances here.

> [!NOTE]
> Make sure to try-catch the write call, as it will rethrow the error back to
> the caller after the retry attempts are up.
>
> Additionally, make sure to gracefully handle and write call rejections, as
> the file will temporarily be locked while packet groups are in being appended
> into the binary, as those require updating a previous section of the blob file.


## Benchmarks
Benchmarks can be found [here](BENCHMARKS.md). Only performance-critical
aspects of the application will be benchmarked.

## Acknowledgements
YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of
[YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[actions-badge]: https://github.com/blurite/rsprot/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprot/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
