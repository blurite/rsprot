package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.message.OutgoingGameMessage

public class NpcInfoSmall(public val buffer: ByteBuf) : OutgoingGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NpcInfoSmall

        return buffer == other.buffer
    }

    override fun hashCode(): Int {
        return buffer.hashCode()
    }

    override fun toString(): String {
        return "NpcInfoSmall(buffer=$buffer)"
    }
}
