package net.rsprot.protocol.js5.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.message.OutgoingJs5Message

/**
 * Js5 group responses are used to feed the cache to the client through the server.
 * @param buffer the byte buffer that is used for the response
 * @property offset the starting index from which the response is written
 * @property length the length of the message to be written
 */
public class Js5GroupResponse(
    buffer: ByteBuf,
    public val offset: Int,
    public val length: Int,
    public val key: Int,
) : DefaultByteBufHolder(buffer),
    OutgoingJs5Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Js5GroupResponse

        if (offset != other.offset) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + offset
        result = 31 * result + length
        return result
    }

    override fun toString(): String =
        "Js5GroupResponse(" +
            "offset=$offset, " +
            "length=$length" +
            ")"
}
