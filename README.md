# RSProt

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit] [![OldSchool - 224 (Alpha)](https://img.shields.io/badge/OldSchool-224_(Alpha)-9a1abd)](https://github.com/blurite/rsprot/tree/master/protocol/osrs-224/osrs-224-api/src/main/kotlin/net/rsprot/protocol/api) [![OldSchool - 223 (Alpha)](https://img.shields.io/badge/OldSchool-223_(Alpha)-9a1abd)](https://github.com/blurite/rsprot/tree/master/protocol/osrs-223/osrs-223-api/src/main/kotlin/net/rsprot/protocol/api) [![OldSchool - 222 (Alpha)](https://img.shields.io/badge/OldSchool-222_(Alpha)-9a1abd)](https://github.com/blurite/rsprot/tree/master/protocol/osrs-222/osrs-222-api/src/main/kotlin/net/rsprot/protocol/api) [![OldSchool - 221 (Alpha)](https://img.shields.io/badge/OldSchool-221_(Alpha)-9a1abd)](https://github.com/blurite/rsprot/tree/master/protocol/osrs-221-api/src/main/kotlin/net/rsprot/protocol/api)

## Status
> [!NOTE]
> This library is currently in an alpha release stage. Breaking changes may be
> done in the future.

## Alpha Usage

The artifacts can be found on [Maven Central](https://central.sonatype.com/search?q=rsprot).

In order to add it to your server, add the below line under dependencies
in your build.gradle.kts.

```kts
implementation("net.rsprot:osrs-224-api:1.0.0-ALPHA-20240906")
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
This library currently supports revision 221, 222, 223 and 224 OldSchool desktop clients.

## Quick Guide
This section covers a quick guide for how to use the protocol after implementing
the base API. It is not a guide for the base API itself, that will come in the
future. This specific quick guide refers to revision 222, which brought changes
to the complicated info packets.
Revision 223 and 224 are almost entirely the same, and is recommended to be used over 222.

#### Player Initialization
When a player logs in, a new protocol instance must be allocated for
player info, npc info and world entity info. This will be used to keep track
of state throughout the session.
These can be allocated via
```kotlin
val playerInfo = service.playerInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
val npcInfo = service.npcInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
val worldEntityInfo = service.worldEntityInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
```
Furthermore, the local coordinate of the player must be updated in all
these info classes - this can be done via:
```kotlin
playerInfo.updateCoord(level, x, z)
npcInfo.updateCoord(currentWorldEntityId, level, x, z)
worldEntityInfo.updateCoord(currentWorldEntityId, level, x, z)
```

After the player logs out, all these info instances **must** be returned back
into the protocol. This can be achieved via:
```kotlin
service.playerInfoProtocol.dealloc(playerInfo)
service.npcInfoProtocol.dealloc(npcInfo)
service.worldEntityInfoProtocol.dealloc(worldEntityInfo)
```

#### Updating Infos
Before we can update any info protocols, we must update the state in them.
This can be done as:
```kotlin
val (x, z, level) = player.coord
worldEntityInfo.updateCoord(currentWorldEntityId, level, x, z)

// Update the camera POV
if (currentWorldEntityId != -1) {
    val entity = World.getWorldEntity(currentWorldEntityId)
    playerInfo.setRenderCoord(entity.level, entity.x, entity.z)
    worldEntityInfo.setRenderCoord(entity.level, entity.x, entity.z)
}
playerInfo.updateCoord(level, x, z)
// Lastly, if build area has changed, we must inform worldEntityInfo
// about the changes at this step!
```

After the state has been updated, the world entity info protocol can be computed.
This can be achieved via a single call as:
```kotlin
service.worldEntityInfoProtocol.update()
```

After the world entity info has processed, we will know about the worlds that
we must handle. In this step, we must deallocate player and npc infos for
any removed worlds, allocate new player and npc infos for any added worlds,
and update the state for every player and npc info. This can be done via:
```kotlin
// First off, write the world entity info to the client - it must
// be aware of the updates before receiving the rebuild world entity packets
packets.worldEntityInfo(worldEntityInfo)

// As we are performing rebuilds further below, we must set the active world
// as the root world - nesting world entities is not possible.
packets.setActiveWorld(-1, player.level)
for (index in worldEntityInfo.getRemovedWorldEntityIndices()) {
    playerInfo.destroyWorld(index)
    npcInfo.destroyWorld(index)
}
for (index in worldEntityInfo.getAddedWorldEntityIndices()) {
    playerInfo.allocateWorld(index)
    npcInfo.allocateWorld(index)

    // Perform the actual world entity update, building the entity
    // in the root world.
    rebuildWorldEntity(index)
}
for (index in worldEntityInfo.getAllWorldEntityIndices()) {
    val entity = World.getWorldEntity(index)
    val (instanceX, instanceZ, instanceLevel) = entity.instanceCoord
    // For dynamic worlds, we want to update the coord to point to the
    // south-western corner of the instance at which the world entity lies.
    npcInfo.updateCoord(index, instanceLevel, instanceX, instanceZ)
    playerInfo.updateRenderCoord(index, instanceLevel, instanceX, instanceZ)
}
if (currentWorldEntityId != -1) {
    // If the player is currently on a world entity, we must mark the
    // origin point as the south-western corner of that instance for the root world
    val entity = World.getWorldEntity(currentWorldEntityId)
    val (instanceX, instanceZ, instanceLevel) = entity.instanceCoord
    npcInfo.updateCoord(-1, instanceLevel, instanceX, instanceZ)
    playerInfo.updateRenderCoord(-1, instanceLevel, instanceX, instanceZ)
} else {
    // If the player is not on a dynamic world entity, we can set the
    // origin point as the local player coordinate
    val (x, z, level) = player.coord
    npcInfo.updateCoord(-1, level, x, z)
    playerInfo.updateRenderCoord(-1, level, x, z)
}
```

Once all the state has been updated for all the infos, we can compute player
and npc infos for all the players. This can be done via:
```kotlin
service.playerInfoProtocol.update()
service.npcInfoProtocol.update()
```

Now that everything is ready, we can perform the last part of the update.
In this section, we send player and npc info packets for every world that's
currently being tracked. This can be done as:
```kotlin
// For dynamic worlds, the npc info origin coord should point to the
// south-western corner of the instance, which corresponds to 0,0 in build area.
packets.setNpcUpdateOrigin(0, 0)
for (index in worldEntityInfo.getAllWorldEntityIndices()) {
    packets.setActiveWorld(index, player.level)
    packets.playerInfo(index, playerInfo)
    packets.npcInfo(index, npcInfo)
    // Send zone updates for this world entity
}

// Lastly, update the root world itself
packets.setActiveWorld(-1, player.level)
packets.playerInfo(PlayerInfo.ROOT_WORLD, playerInfo)
if (currentWorldEntityId != -1) {
    val entity = World.getWorldEntity(currentWorldEntityId)
    val localCoords = this.buildAreaManager.local(entity.coord)
    packets.setNpcUpdateOrigin(localCoords.x, localCoords.z)
} else {
    // If the player is in the root world, this should correspond
    // to the player's current coordinate in the build area.
    val localCoords = this.buildAreaManager.local(coord)
    packets.setNpcUpdateOrigin(localCoords.x, localCoords.z)
}
packets.npcInfo(NpcInfo.ROOT_WORLD, npcInfo)
```

> [!IMPORTANT]
> The `toPacket()` function must be called on all info packets after they have
> been calculated. This function __may__ result in an exception being thrown.
> In such cases, the exception happened when the info was calculated.
> If successful, write the result of the `toPacket()` function call to the session.

#### Map Reload
Whenever a map reload occurs, we must inform the protocol of the build area.
Both the player info and npc info protocols will require doing so:
```kotlin
playerInfo.updateBuildArea(worldId, BuildArea(southWestZoneX, southWestZoneZ))
npcInfo.updateBuildArea(worldId, BuildArea(southWestZoneX, southWestZoneZ))
```

Any player or NPC that does not fit into the build area will not be added
to high resolution view. Furthermore, as the client trims off a one tile border,
so do we. On the northern and eastern border, two tiles get cut off -
this is because the client doesn't properly render entities there, even though
map renders fine.

### NPCs
For each NPC that spawns into the world, an avatar must be allocated.
This can be done via:
```kotlin
val avatar = service.npcAvatarFactory.alloc(
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
`service.npcAvatarFactory.release(avatar)`.
If a NPC dies but is set to respawn, instead just mark the avatar inaccessible.

### World Entities
For each world entity that spawns into the world, an avatar must be allocated.
This can be done via:
```kotlin
val avatar = service.worldEntityAvatarFactory.alloc(
    index,
    sizeX,
    sizeZ,
    coord.x,
    coord.z,
    coord.level,
    angle,
)
```
Movement and turning will be controlled via this avatar.
When the world entity despawns, the avatar must be returned back into the
protocol. This can be done via:
`service.worldEntityAvatarFactory.release(avatar)`

## Changes

### Revision 224
Revision 224 saw the introduction of submenus in interfaces, as well as
more diversity changes.

#### Additions
Two client-to-server packets, and one server-to-client packet was introduced.
1. UPDATE_PLAYER_MODEL
2. IF_SUBOP
3. UNKNOWN

The update player model is a newer 224-specific variant of the previous
UPDATE_PLAYER_MODEL, which was renamed to UPDATE_PLAYER_MODEL_OLD.
Neither of these packets is actually in use. The new addition is also riddled
with bugs: In the C++ client, the revision check if >= 24 rather than >= 224,
and the size defined for this packet is 26, when the sum of the payload only
adds up to 20 bytes. As this packet is completely bugged, no variant of this
was implemented in RSProt in the current stage.

The unknown packet, while sent and available in the java client, is currently
unidentified. Only empty strings are ever sent in it, and the property only
gets passed to a clientscript instruction. There are no clientscripts that
would use this property right now. Due to the lack of information surrounding
this packet right now, the packet will not be implemented yet. Once we know
what it is for, support will be added retroactively.

### Revision 223
Not many changes occurred in revision 223 - four new packets were introduced,
one of which is an updated variant of an older one.

#### Deprecations
CAM_TARGET_OLD has been deprecated. This class was previously called CAM_TARGET,
but Jagex has introduced a new variant of this that supports passing in the
index of the player to focus on when on a world entity. Previously, you could
only focus on a world entity itself.

#### Additions
Three brand-new packets were added:
1. HIDENPCOPS
2. HIDEOBJOPS
3. HIDELOCOPS

All these packets will hide right-click options 1-5 on any of the respective
elements. The examine option, if present, will still render, however.
The intended purpose here is to make moving on a ship more convenient,
as mis-clicks on random things are quite easy to occur while at the wheel of
a ship.

### Revision 222
In revision 222, Jagex released the first prototype of the tech used for sailing.
As such, quite significant changes were done to player and NPC info packets.

#### Packets
A total of 6 new packets were introduced with revision 222, in the server to
client direction. No other changes were performed to other packets in either
direction.
- CAM_TARGET: This packet can be used to focus the camera on any player,
NPC or world entity in the root world. It will only work on entities which
are visible to the local player, and will fall back to the local player
in the root world if the respective entity cannot be found.
- CLEAR_ENTITIES: A packet which can be used to destroy a new world entity
in the client's perspective, removing any NPCs and nested world entities
as a result of it.
- SET_ACTIVE_WORLD: Marks a specific world active, meaning the updates which
rely on the active world that follow will be applied to this world.
- SET_NPC_UPDATE_ORIGIN: Sets the origin point for the next NPC info that
will follow. As of this revision, NPC info is no longer attached to the relative
position of the local player - instead it must be transmitted via the origin
point. This coordinate should point to the local player coordinate in the build
area for root world updates - it needs to be different for world entities, though.
- WORLDENTITY_INFO: A new info protocol used to keep track of any world entities
for a given player. This packet is quite similar to player and NPC info,
except it does not have any extended info support.
- REBUILD_WORLDENTITY: A packet to draw an instance into the build area of
the player, effectively rendering a specific world entity to the player.


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

## Design Choices

### Memory-Optimized Messages
A common design choice throughout this library will be to utilize smaller data types wherever applicable.
The end-user will always get access to normalized messages though.

Below are two examples of the same data structure, one in a compressed data structure, another in a traditional data class:
<details>
  <summary>Compressed HostPlatformStats</summary>

```kt
public class HostPlatformStats(
    private val _version: UByte,
    private val _osType: UByte,
    public val os64Bit: Boolean,
    private val _osVersion: UShort,
    private val _javaVendor: UByte,
    private val _javaVersionMajor: UByte,
    private val _javaVersionMinor: UByte,
    private val _javaVersionPatch: UByte,
    private val _unknownConstZero1: UByte,
    private val _javaMaxMemoryMb: UShort,
    private val _javaAvailableProcessors: UByte,
    public val systemMemory: Int,
    private val _systemSpeed: UShort,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    private val _gpuDriverMonth: UByte,
    private val _gpuDriverYear: UShort,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    private val _cpuCount1: UByte,
    private val _cpuCount2: UByte,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
) {
    public val version: Int
        get() = _version.toInt()
    public val osType: Int
        get() = _osType.toInt()
    public val osVersion: Int
        get() = _osVersion.toInt()
    public val javaVendor: Int
        get() = _javaVendor.toInt()
    public val javaVersionMajor: Int
        get() = _javaVersionMajor.toInt()
    public val javaVersionMinor: Int
        get() = _javaVersionMinor.toInt()
    public val javaVersionPatch: Int
        get() = _javaVersionPatch.toInt()
    public val unknownConstZero: Int
        get() = _unknownConstZero1.toInt()
    public val javaMaxMemoryMb: Int
        get() = _javaMaxMemoryMb.toInt()
    public val javaAvailableProcessors: Int
        get() = _javaAvailableProcessors.toInt()
    public val systemSpeed: Int
        get() = _systemSpeed.toInt()
    public val gpuDriverMonth: Int
        get() = _gpuDriverMonth.toInt()
    public val gpuDriverYear: Int
        get() = _gpuDriverYear.toInt()
    public val cpuCount1: Int
        get() = _cpuCount1.toInt()
    public val cpuCount2: Int
        get() = _cpuCount2.toInt()
}
```
</details>

<details>
  <summary>Traditional HostPlatformStats</summary>

```kt
public data class HostPlatformStats(
    public val version: Int,
    public val osType: Int,
    public val os64Bit: Boolean,
    public val osVersion: Int,
    public val javaVendor: Int,
    public val javaVersionMajor: Int,
    public val javaVersionMinor: Int,
    public val javaVersionPatch: Int,
    public val unknownConstZero1: Int,
    public val javaMaxMemoryMb: Int,
    public val javaAvailableProcessors: Int,
    public val systemMemory: Int,
    public val systemSpeed: Int,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    public val gpuDriverMonth: Int,
    public val gpuDriverYear: Int,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    public val cpuCount1: Int,
    public val cpuCount2: Int,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
)
```
 </details>

> [!IMPORTANT]
> There is a common misconception among developers that types on heap smaller than ints are only useful in their respective primitive arrays.
> In reality, this is only sometimes true. There are a lot more aspects to consider. Below is a breakdown on the differences.


<details>
  <summary>Memory Alignment Breakdown</summary>

[JVM's memory alignment](https://www.baeldung.com/java-memory-layout) is the reason why we prioritize compressed messages over traditional ones.
It is commonly believed that primitives like bytes and shorts do not matter on the heap and end up consuming the same amount of memory as an int,
but this is simply not true. The object itself is subject to memory alignment and will be padded to a specific amount of bytes as a whole.
Given this information, we can see the stark differences between the two objects by adding up the memory usage of each of the properties.
For this example, we will assume all the strings are empty and stored in the
[JVM's string constant pool](https://www.baeldung.com/java-string-constant-pool-heap-stack), so we only consider the reference of those.
The cpuFeatures array is a size-3 int array.
By adding up all the properties of the compressed variant of the HostPlatformStats, we come to the following results:
| Type | Count | Data Size (bytes) |
| --- | --- | --- |
| byte | 11 | 1 |
| boolean | 1 | 1 |
| short | 4 | 2 |
| int | 2 | 4 |
| intarray | 1 | Special |
| reference | 8 | Special |

By adding up all the data types, we come to a sum of (11 x 1) + (1 x 1) + (4 x 2) + (2 x 4) + (1 x intarray) + (8 x reference),
which adds up to 28 + (1 x intarray) + (8 x reference) bytes.

However, now, let's look at the traditional variant:
| Type | Count | Data Size (bytes) |
| --- | --- | --- |
| int | 18 | 4 |
| intarray | 1 | Special |
| reference | 8 | Special |

The total adds up to (18 x 4) + (1 x intarray) + (8 x reference),
which adds up to 72 + (1 x intarray) + (8 x reference) bytes.

<ins>So, what about the special types?</ins>

This is where things become less certain. It is down to the JVM and the amount of memory allocated to the JVM process.

On a 32-bit JVM, the memory layout looks like this:
| Type | Data Size (bytes) |
| --- | --- |
| Object Header | 8 |
| Object Reference | 4 |
| Byte Alignment | 4 |

On a 64-bit JVM, the memory layout is as following:
| Type | Data Size (bytes) |
| --- | --- |
| Object Header | 12 |
| Object Reference (xmx <= 32gb, compressed OOPs[^1]) | 4 |
| Object Reference (xmx > 32gb) | 8 |
| Byte Alignment | 8 |

So, how much do our HostPlatformStats objects consume in the end?
If we assume we are on a 64-bit JVM with the maximum heap size set to 32GB or less, the object memory consumption boils down to the following:
From the earlier example, the intarray will consume 12 + (3 * 4) bytes, and the string references will consume 4 bytes each.
So if we now add these values up, we come to a total of:
Compressed HostPlatformStats: 84 bytes
Traditional HostPlatformStats: 128 bytes
Due to the JVM's 8 byte alignment however, all objects are aligned to consume a multiple of 8 bytes.
In this scenario, because our compressed implementation comes to 84 bytes, which is not a multiple-of-8 bytes, a small waste occurs.
The JVM will allocate 4 extra bytes to fit the 8-byte alignment constraint, giving us a total of 88 bytes consumed.
In the case of the traditional implementation, since it is already a multiple of 8, it will remain as 128 bytes.

 </details>

> [!NOTE]
> The reason we prefer compressed implementations is to reduce the memory footprint of the library. As demonstrated above,
> the compressed implementation consumes 31.25% less memory than the traditional counterpart.
> While the compressed code may be harder to read and take longer to implement, this is a one-time job as the models rarely change.
> On the larger scale, this could result in a considerably smaller footprint of the library for servers, and less work for garbage collectors.

## Benchmarks
Benchmarks can be found [here](BENCHMARKS.md). Only performance-critical
aspects of the application will be benchmarked.

[^1]: [Compressed ordinary object pointers](https://www.baeldung.com/jvm-compressed-oops) are a trick utilized by the 64-bit JVM to compress object references into 4 bytes instead of the traditional 8. This is only possible if the Xmx is set to 32GB or less. Since Java 7, compressed OOPs are enabled by default if available.

[actions-badge]: https://github.com/blurite/rsprot/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprot/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
