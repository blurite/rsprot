package net.rsprot.protocol.game.outgoing.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Var clan enable packet is used to initialize a new var domain
 * in the client, intended to be sent as the player joins a clan.
 */
public data object VarClanEnable : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
