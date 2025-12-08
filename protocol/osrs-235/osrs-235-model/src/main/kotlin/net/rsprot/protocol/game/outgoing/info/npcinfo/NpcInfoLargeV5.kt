package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf

/**
 * A large npc info wrapper packet, used to wrap the pre-built buffer from the npc info class.
 */
public class NpcInfoLargeV5(
    buffer: ByteBuf,
) : NpcInfoPacket(buffer) {
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
        return "NpcInfoLargeV5()"
    }
}
