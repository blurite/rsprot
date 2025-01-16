package net.rsprot.protocol.js5.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.message.OutgoingJs5Message

/**
 * Js5 group responses are used to feed the cache to the client through the server.
 * @param buffer the byte buffer that is used for the response
 */
public class Js5GroupResponse(
    buffer: ByteBuf,
    public val key: Int,
) : DefaultByteBufHolder(buffer),
    OutgoingJs5Message {
    override fun estimateSize(): Int {
        return content().readableBytes()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Js5GroupResponse) return false
        if (!super.equals(other)) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + key
        return result
    }

    override fun toString(): String {
        return "Js5GroupResponse(" +
            "key=$key" +
            ")"
    }
}
