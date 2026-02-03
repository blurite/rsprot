package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoPacket
import net.rsprot.protocol.game.outgoing.info.npcinfo.SetNpcUpdateOrigin
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoPacket
import net.rsprot.protocol.game.outgoing.info.util.PacketResult
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfoV7Packet
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorldV2

/**
 * A class that holds all the packets and properties necessary to update the root world.
 * @property activeWorld the current active level. If the player is on a world entity,
 * this will be equal to whatever level the world entity is projected on. Otherwise, the
 * player's current level is chosen.
 * @property activeWorld the set-active-world-v2 packet to inform the client which level to update
 * in the root world.
 * @property npcUpdateOrigin the origin offsets for npc info in the root world. These have been
 * matched up with the calculations inside the packet to ensure consistency.
 * @property worldEntityInfo a result of the world entity info packet call.
 * @property playerInfo a result of the player info packet call.
 * @property npcInfo a result of the npc info packet call.
 *
 * Note that if any of the three info packets are unsuccessful, the player should realistically be
 * kicked offline, as there's no recovery due to it being a massive state machine.
 */
public class RootWorldInfoPackets(
    public val activeLevel: Int,
    public val activeWorld: SetActiveWorldV2,
    public val npcUpdateOrigin: SetNpcUpdateOrigin,
    public val worldEntityInfo: PacketResult<WorldEntityInfoV7Packet>,
    public val playerInfo: PacketResult<PlayerInfoPacket>,
    public val npcInfo: PacketResult<NpcInfoPacket>,
) {
    public val worldId: Int
        get() = -1
}
