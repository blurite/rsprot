package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam reset is used to clear out any camera shaking or
 * any sort of movements that might've been previously set.
 * Additionally, unlocks the camera if it has been locked in place.
 */
public data object CamReset : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT
}
