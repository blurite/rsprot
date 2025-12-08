package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoPacket
import net.rsprot.protocol.game.outgoing.info.npcinfo.SetNpcUpdateOrigin
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorldV2

/**
 * A set of packets and properties for a given dynamic world.
 * @property worldId the id of the world that is being updated.
 * @property activeLevel the level of the world that is being updated. This will be equal
 * to player's current level if this is the world on which the player currently resides,
 * otherwise it is the world's own active level.
 * @property added whether this world was freshly added (and [net.rsprot.protocol.game.outgoing.map.RebuildWorldEntityV2]
 * should be performed on it, along with a full zone synchronization).
 * @property activeWorld the active world packet necessary to inform the client of the coming update.
 * @property npcUpdateOrigin the offsets for npc info packet in this world. This is a cached packet
 * of the 0,0 values as that is what we calculate it against at all times.
 * @property npcInfo a result of the npc info packet. It should be noted that if this result is
 * unsuccessful, the player should be kicked offline, as it is not really possible to recover from it.
 * Alternative would be to destroy the world itself, but that then affects everyone else nearby too.
 */
public class WorldInfoPackets(
    public val worldId: Int,
    public val activeLevel: Int,
    public val added: Boolean,
    public val activeWorld: SetActiveWorldV2,
    public val npcUpdateOrigin: SetNpcUpdateOrigin,
    public val npcInfo: Result<NpcInfoPacket>,
)
