package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * A large npc info wrapper packet, used to wrap the pre-built buffer from the npc info class.
 */
public class NpcInfoLarge(buffer: ByteBuf) : DefaultByteBufHolder(buffer), OutgoingGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "NpcInfoLarge() ${super.toString()}"
    }
}
