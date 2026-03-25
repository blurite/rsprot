package net.rsprot.protocol.game.outgoing.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Var clan disable packet is used to clear out a var domain
 * in the client, intended to be sent as the player leaves a clan.
 */
public data object VarClanDisable : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
