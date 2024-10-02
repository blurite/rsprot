package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.ConsumableMessage
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * A npc info packet class, wrapped in its own byte buf holder as the packet encoder is only
 * invoked through Netty threads, therefore it is not safe to strictly pass the reference
 * from player info itself.
 */
public class PlayerInfoPacket(
    buffer: ByteBuf,
) : DefaultByteBufHolder(buffer),
    OutgoingGameMessage,
    ConsumableMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    private var consumed: Boolean = false

    override fun consume() {
        this.consumed = true
    }

    override fun isConsumed(): Boolean = this.consumed

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = super.toString()
}
