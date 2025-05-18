package net.rsprot.protocol.game.outgoing.worldentity

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Clear entities packet is used to clear any NPCs and world entities from the currently
 * active world. It is important to note that the [net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo.clearEntities],
 * [net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfo.clearEntities] and
 * [net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.update] functions must be
 * called in conjunction with this packet.
 * Additionally, this packet __must__ be called before the respective
 * [net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol.update],
 * [net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol.update] and
 * [net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.update] functions.
 */
public data object ClearEntities : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
