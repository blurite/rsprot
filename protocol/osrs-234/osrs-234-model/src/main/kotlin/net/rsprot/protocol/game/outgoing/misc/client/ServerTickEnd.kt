package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Server tick end packets are used by the C++ client
 * for ground item settings, in order to decrement
 * visible ground item's timers. Without it, all ground
 * items' timers will remain frozen once dropped.
 */
public data object ServerTickEnd : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT
}
