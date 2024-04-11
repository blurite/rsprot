package net.rsprot.protocol.game.outgoing.clan

import net.rsprot.protocol.message.OutgoingMessage

/**
 * Var clan enable packet is used to initialize a new var domain
 * in the client, intended to be sent as the player joins a clan.
 */
public data object VarClanEnable : OutgoingMessage
