package net.rsprot.protocol.game.incoming.misc.user

import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Close modal messages are sent when the player clicks on the 'x' button
 * of a modal interface, or if they press the 'Esc' key while having the
 * "Esc to close interfaces" setting enabled.
 */
public data object CloseModal : IncomingGameMessage
