package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * ZBuf packet is used to toggle depth buffering in client.
 * @property enabled whether to enable depth buffering.
 */
public class ZBuf(
    public val enabled: Boolean,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZBuf

        return enabled == other.enabled
    }

    override fun hashCode(): Int = enabled.hashCode()

    override fun toString(): String = "ZBuf(enabled=$enabled)"
}
