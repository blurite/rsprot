package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.ConsumableMessage
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * A small npc info wrapper packet, used to wrap the pre-built buffer from the npc info class.
 */
public class NpcInfoSmallV5(
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
