package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.message.OutgoingMessage

/**
 * Server tick end packets are used by the C++ client
 * for ground item settings, in order to decrement
 * visible ground item's timers. Without it, all ground
 * items' timers will remain frozen once dropped.
 */
public data object ServerTickEnd : OutgoingMessage
