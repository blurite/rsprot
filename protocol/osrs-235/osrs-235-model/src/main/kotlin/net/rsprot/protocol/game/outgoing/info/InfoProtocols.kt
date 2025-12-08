package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol
import net.rsprot.protocol.internal.checkCommunicationThread

public class InfoProtocols(
    public val playerInfoProtocol: PlayerInfoProtocol,
    public val npcInfoProtocol: NpcInfoProtocol,
    public val worldEntityInfoProtocol: WorldEntityProtocol,
) {
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

    public fun dealloc(infos: Infos) {
        playerInfoProtocol.dealloc(infos.playerInfo)
        npcInfoProtocol.dealloc(infos.npcInfo)
        worldEntityInfoProtocol.dealloc(infos.worldEntityInfo)
    }
}
