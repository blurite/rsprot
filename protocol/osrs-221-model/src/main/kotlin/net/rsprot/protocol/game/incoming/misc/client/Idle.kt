package net.rsprot.protocol.game.incoming.misc.client

import net.rsprot.protocol.message.IncomingMessage

/**
 * Idle messages are sent if the user hasn't interacted with their
 * mouse nor their keyboard for 15,000 client cycles (20ms/cc) in a row,
 * meaning continuous inactivity for five minutes in a row.
 */
public data object Idle : IncomingMessage
