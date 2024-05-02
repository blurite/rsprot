package net.rsprot.protocol.api.encoder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.Prot
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

public abstract class OutgoingMessageEncoder : MessageToByteEncoder<OutgoingMessage>(OutgoingMessage::class.java) {
    protected abstract val cipher: StreamCipher
    protected abstract val repository: MessageEncoderRepository<*>

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        val encoder = repository.getEncoder(msg::class.java)
        val prot = encoder.prot
        val opcode = prot.opcode
        if (opcode < 0x80) {
            out.p1(opcode + cipher.nextInt())
        } else {
            out.p1((opcode ushr 8 or 0x80) + cipher.nextInt())
            out.p1((opcode and 0xFF) + cipher.nextInt())
        }

        val sizeMarker =
            when (prot.size) {
                Prot.VAR_BYTE -> {
                    out.p1(0)
                    out.writerIndex() - 1
                }
                Prot.VAR_SHORT -> {
                    out.p2(0)
                    out.writerIndex() - 2
                }
                else -> -1
            }

        val payloadMarker = out.writerIndex()
        encoder.encode(
            ctx,
            out.toJagByteBuf(),
            msg,
        )

        // Update the size based on the number of bytes written, if it's a var-* packet
        if (sizeMarker != -1) {
            val writerIndex = out.writerIndex()
            var length = writerIndex - payloadMarker
            if (msg is LoginResponse.Ok) {
                // TODO: Figure out a nicer way to deal with this?
                // Ok login response needs to include 3 bytes extra as it immediately reads
                // and discards the header of the first packet that comes in (rebuild login)
                length += 3
            }
            out.writerIndex(sizeMarker)
            when (prot.size) {
                Prot.VAR_BYTE -> out.p1(length)
                Prot.VAR_SHORT -> out.p2(length)
            }
            out.writerIndex(writerIndex)
        }
    }
}
