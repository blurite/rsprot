package net.rsprot.protocol.game.incoming.misc.user

import net.rsprot.protocol.ProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Oculus leave message is sent when the player presses the 'Esc' key
 * to exit the orb of oculus view.
 */
public data object OculusLeave : IncomingGameMessage {
    override val category: ProtCategory
        get() = GameClientProtCategory.USER_EVENT
}
