package net.rsprot.protocol.game.outgoing.clan

import net.rsprot.protocol.message.OutgoingMessage

/**
 * Var clan disable packet is used to clear out a var domain
 * in the client, intended to be sent as the player leaves a clan.
 */
public data object VarClanDisable : OutgoingMessage
