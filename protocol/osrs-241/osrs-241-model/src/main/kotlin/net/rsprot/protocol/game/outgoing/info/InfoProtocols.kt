package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol
import net.rsprot.protocol.internal.checkCommunicationThread

/**
 * A combination class for the three info protocols, making it easier for servers
 * to consume and keep everything up to date.
 */
public class InfoProtocols(
    public val playerInfoProtocol: PlayerInfoProtocol,
    public val npcInfoProtocol: NpcInfoProtocol,
    public val worldEntityInfoProtocol: WorldEntityProtocol,
) {
    /**
     * Allocates a combination info object allowing the server to communicate through a single
     * means, rather than each one individually.
     * @param idx the index of the player that is allocating the infos.
     * @param oldSchoolClientType the client type used by the player.
     */
    public fun alloc(
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    ): Infos {
        checkCommunicationThread()
        val worldEntityInfo = worldEntityInfoProtocol.alloc(idx, oldSchoolClientType)
        return Infos(
            playerInfoProtocol.alloc(idx, oldSchoolClientType, worldEntityInfo),
            npcInfoProtocol.alloc(idx, oldSchoolClientType, worldEntityInfo),
            worldEntityInfo,
        )
    }

    /**
     * Deallocates the provided info object.
     * @param infos the infos previously allocated.
     */
    public fun dealloc(infos: Infos) {
        playerInfoProtocol.dealloc(infos.playerInfo)
        npcInfoProtocol.dealloc(infos.npcInfo)
        worldEntityInfoProtocol.dealloc(infos.worldEntityInfo)
    }

    /**
     * Performs a full update across world entity info, player info and npc info,
     * in the provided order.
     */
    public fun update() {
        worldEntityInfoProtocol.update()
        playerInfoProtocol.update()
        npcInfoProtocol.update()
    }
}
