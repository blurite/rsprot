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
implementation("net.rsprot:osrs-235-api:1.0.0-ALPHA-20251118")
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
future. This specific quick guide refers to revision 235.

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

### Revision 235
Revision 235 brings no protocol level changes.
However, as a small side note, OBJ packets are no longer limited to id 32767
in client.

### Revision 234

#### Additions
- ZBUF - a server-to-client packet which enables or disables depth buffer in client.
- Worldentity info's extended info - sequence; allowing server to modify the
currently playing animation of a worldentity at runtime.

#### Removals
- The following server prots have been removed (These packets can now only be sent as part of UPDATE_ZONE_PARTIAL_ENCLOSED):
  - OBJ_ADD
  - OBJ_DEL
  - OBJ_COUNT
  - OBJ_ENABLED_OPS
  - OBJ_CUSTOMISE
  - OBJ_UNCUSTOMISE
- WORLDENTITY_INFO_V5 was removed.

### Revision 233

#### Additions
- ACCOUNT_FLAGS - a server-to-client packet which sets flags for client to use.
- OPWORLDENTITY* - Adds interaction support with world entities, same as locs, npcs etc.

#### Changes
- WORLDENTITY_INFO_V6 - Adds support for extended info in world entities. Currently
only the visible-ops flag is implemented.

### Revision 232

#### Additions
Revision 232 adds new variants of obj packets, ones which aren't zone-local,
instead transmitting the absolute, global coordinate. It is unclear at this
time whether these packets are _SPECIFIC variants, or intended as full
replacements to the existing zone packets. This will be determined when
revision 232 actually rolls out in the server.

The newly added packets:
- OBJ_ADD_SPECIFIC
- OBJ_DEL_SPECIFIC
- OBJ_COUNT_SPECIFIC
- OBJ_ENABLED_OPS_SPECIFIC
- OBJ_CUSTOMISE_SPECIFIC
- OBJ_UNCUSTOMISE_SPECIFIC

#### Changes
EVENT_MOUSE_CLICK packet was changed to be uniform between the Native and
Java clients. The old EVENT_NATIVE_MOUSE_CLICK packed is no longer in use,
and instead a new variant was created that will be used by both ends when
revision 232 rolls out in server.

#### Removals
A few now-unused packets were removed with this revision:
- IF_SET_EVENTS_V1
- IF_RESYNC_V1
- MAP_PROJANIM_V1
- PROJANIM_SPECIFIC_V3

Additionally, the face-coord NPC extended info is no longer utilized by
the client, and as such, has been removed from RSProt altogether as well.
Clients are expected to use the face-angle extended info instead, which
was first introduced in 231.

### Revision 231
Revision 231 comes with a handful of protocol changes.

#### Additions
Below is a list of additions to __server__ prots.
- PROJANIM_SPECIFIC_V4 - now supports the start and end coordinates as absolutes,
  meaning the packet is no longer limited to within a single world.
  Additionally, the packet no longer multiplies the heights by 4 in the client,
  giving finer precision to servers.
- IF_SETEVENTS_V2 - a new variant of IF_SETEVENTS, supporting up to 32 ifbuttons,
  up from the previous 10.
- IF_RESYNC_V2 - a new variant of IF_RESYNC that adds support for the expanded
  ifbuttons, just like IF_SETEVENTS_V2
- MAP_PROJANIM_V2 - a new variant of the regular MAP_PROJANIM, supporting
  the end as an absolute coordinate, not relative to the start, allowing
  the projectile to be shot across-worlds.
  Additionally, the packet no longer multiplies the heights by 4 in the client,
  giving finer precision to servers.
  Unlike every other packet, this one does not come with a ServerProt instance,
  and can only be transmitted through UPDATE_ZONE_PARTIAL_ENCLOSED.
- A new extended info block for NPC_INFO - face angle. The old face coord
variant is still available for the time being, but is likely going to be removed
in the future. The new face angle variant works nearly the same as for players,
but comes with the 'instant' boolean property.

Below is a list of additions to __client__ prots.
- IF_BUTTONX - A new variant of IF_BUTTON(1-10) that sends the button id
  inside the packet. It now supports button ids 1 through 32.
- IF_RUNSCRIPT - A way of invoking a serverscript in the server, by the client.
  The exact usages of it are currently unknown, but it effectively works the same
  way as RUN_CLIENTSCRIPT does for server -> client.

#### Removals
- SET_ACTIVE_WORLD_V1


### Revision 230
Revision 230 was primarily a cleanup revision, with no new packets or changes
introduced.

#### Removals
- CLEAR_ENTITIES
- WORLDENTITY_INFO_V4

### Revision 229
Revision 229 brings the following changes:

#### Additions
1. SET_ACTIVE_WORLD_V2: A simplified variant of the V1 packet.
2. An unknown var-short packet: Good chance this is the removed
WORLDENTITY_INFO_V3 packet, but it's hard to say with certainty.
It is unused on both Java and C++ clients, and only reads g1()
which it discards.

#### Removals
1. LOC_ADD_CHANGE_V1: Removed as revision 228 introduced the V2 variant
which has been in use since then.
2. WORLDENTITY_INFO_V3: Removed since V4 has been in use starting with
revision 227.

#### Changes
1. CAMERA_TARGET_V2 has now been migrated to V3, and structural changes
have been done to the packet.
2. Login CRCs now transmit 23 CRCs instead of the previous 21.
REMAINING_BETA_ARCHIVES has gone from opcode 20 to 32, as the client
still needs to support last revision's variant.

### Revision 228
Revision 228 comes with very little changes, only introducing a single
new server packet. This revision does not delete any other packets, or
change almost anything else in the client, even outside of networking.

#### Additions
1. LOC_ADD_CHANGE_V2: A new variant of LOC_ADD_CHANGE that has support for
server-provided minimenu ops. An example of this would be "Cut-down" on trees,
allowing the server to change that to something completely dynamic.
OldSchool RuneScape is no longer using the older packet, even if no option
overrides are used.

### Revision 227
Revision 227 was mostly a clean-up revision, doing little overall changes.

#### Additions
1. An unknown client packet which is not used on native desktop nor java.
This packet might be used in mobile, we do not know. It has a size of 1 byte.
2. PACKET_GROUP_START: A packet which lets the server tell the client to process
a group of packets all in one client cycle after waiting for the packets to
arrive completely. This packet has one flaw - it is limited to a maximum size of
32767 bytes for the payload (sum of all the children, including their opcodes,
sizes and payloads).
3. WORLDENTITY_INFO_V4: A new variant of world entity info, adding support
for defining the offset for the ship model/center-point in fine client units.

#### Removals
1. WORLDENTITY_INFO_V1 (server)
2. WORLDENTITY_INFO_V2 (server)
3. NPC_INFO_SMALL_V4 (server)
4. NPC_INFO_LARGE_V5 (server)
5. PROJANIM_SPECIFIC_V1 (server)
6. PROJANIM_SPECIFIC_V2 (server)
7. UPDATE_FRIENDCHAT_CHANNEL_FULL_V1 (server)
8. UPDATE_STAT_V1 (server)
9. CAM_TARGET_V1 (server)
10. UPDATE_PLAYER_MODEL_V1 (client)
11. EXAMINE_OBJ_V1 (client)


### Revision 226
Revision 226 was a relatively large protocol change, adding many new
server prot variants, and one extra client prot. In this revision, we also migrated
from the `_OLD` naming scheme to `_V*` versioning scheme, to better reflect
on what Jagex uses.

#### Additions

##### Client Prots
1. SET_HEADING: used to inform the server of the direction in
which a world entity is supposed to move, if in the heading interaction mode.

#### Server Prots
1. SET_INTERACTION_MODE: Used to change the tile and entity interaction modes
in the client, which allows one to disable all clicks, allow walking or
selecting a ship's heading. Additionally, allows one to disable interactions
with any entities (or go in examine-only mode).
2. RESET_INTERACTION_MODE: Undoes the effects of SET_INTERACTION_MODE.
3. WORLDENTITY_INFO_V3: A new variant of worldentity info which reads the coord
as coord fine, rather than coord grid. This allows server to define sub-tile
precision of where a worldentity is supposed to be or go to.
4. NPC_INFO_SMALL_V5: A new variant which uses 6 bits for small NPC info,
rather than the previous 5 when adding new NPCs to high resolution view.
This was intended to increase the render distance for the wilderness world
boss that was supposed to come. A new LARGE variant of this packet exists
also, but that is unchanged compared to the V4 variant.
5. OBJ_CUSTOMISE zone prot, allowing the server to change the colours,
textures and model of an item on the ground.
6. OBJ_UNCUSTOMISE zone prot, resetting any customisations done to an item
on the ground.

### Revision 225
Revision 225 brought a small-scale refactoring to the player info packet,
reducing it back to a single instance rather than one per worldentity.
This means slight changes in the API, but overall the differences are miniscule.
These changes bring significant performance improvements over the past 3
revisions, though.

#### Additions
1. A new server prot for PROJANIM_SPECIFIC, which now includes the source index
property. The old ones are now deprecated.
2. A new server prot for WORLDENTITY_INFO, which has a new byte addition
for the current height level of the world entity, allowing it to teleport up
and down.
3. EVENT_NATIVE_MOUSE_MOVE packet has been restored, and comes with a small
change - it now includes information about a recently pressed mouse button.

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
