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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Js5GroupResponse) return false
        if (!super.equals(o)) return false

        if (key != o.key) return false

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
