package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.message.OutgoingMessage

/**
 * Trigger on dialog abort is used to invoke any ondialogabort
 * scripts that have been set up on interfaces, typically to close
 * any dialogues.
 */
public data object TriggerOnDialogAbort : OutgoingMessage
