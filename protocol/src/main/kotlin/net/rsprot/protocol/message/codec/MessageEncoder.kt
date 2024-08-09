package net.rsprot.protocol.message.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage

public interface MessageEncoder<in T : OutgoingMessage> {
    /**
     * The protocol type of this message, providing us with the expected size of the message.
     */
    public val prot: ServerProt

    /**
     * Encodes the [message] into the [buffer] where the [buffer] is the `out` property in Netty.
     * Rather than allocating new byte buf instances, we can directly encode to the `out` property
     * with most packets. This does not however hold true with pre-computed ones, such as player info,
     * where we must instead write the pre-computed bytes over to `out` first.
     *
     * @param buffer the `out` buffer in Netty encoders into which the data gets directly written.
     * @param message the message to encode into the buffer.
     */
    public fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: T,
    )
}
