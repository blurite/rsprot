package net.rsprot.protocol.game.outgoing.info.entityinfo

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Clear entities packet is used to clear any NPCs and world entities from the currently
 * active world. This furthermore sets the active world back to root.
 * This packet will not clear out any players, so the player info related to that world must
 * still be used to transfer players over.
 */
public data object ClearEntities : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
