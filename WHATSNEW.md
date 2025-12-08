## What's New?

### Revision 235
Revision 235 brings no protocol level changes.
However, as a small side note, OBJ packets are no longer limited to id 32767
in client.

> [!IMPORTANT]
> RSProt received a large-scale refactor in revision 235 and requires numerous
> breaking changes. Refer to the readme to see how to update it.

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
