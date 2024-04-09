package net.rsprot.protocol.game.incoming.buttons

import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If1 button messages are sent whenever a player clicks on an older
 * if1-type component.
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the player interacted with
 */
public class If1Button(
    private val combinedId: CombinedId,
) : IncomingMessage {
    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as If1Button

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int {
        return combinedId.hashCode()
    }

    override fun toString(): String {
        return "If1Button(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
    }
}
