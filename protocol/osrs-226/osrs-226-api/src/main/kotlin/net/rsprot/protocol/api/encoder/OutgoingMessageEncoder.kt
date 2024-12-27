package net.rsprot.protocol.api.encoder

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufHolder
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.EncoderException
import io.netty.util.ReferenceCountUtil
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.Prot
import net.rsprot.protocol.api.handlers.OutgoingMessageSizeEstimator
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.ByteBufHolderWrapperFooterMessage
import net.rsprot.protocol.message.ByteBufHolderWrapperHeaderMessage
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * A generic message encoder for all outgoing messages, including login, JS5 and game.
 * @property cipher the stream cipher used to encrypt the opcodes, and in the case of
 * some packets, the entire payload
 * @property repository the message encoder repository containing all the encoders
 */
public abstract class OutgoingMessageEncoder : ChannelOutboundHandlerAdapter() {
    protected abstract val cipher: StreamCipher
    protected abstract val repository: MessageEncoderRepository<*>
    protected abstract val validate: Boolean
    protected abstract val estimator: OutgoingMessageSizeEstimator

    override fun write(
        ctx: ChannelHandlerContext,
        msg: Any,
        promise: ChannelPromise,
    ) {
        if (msg !is OutgoingMessage) {
            ctx.write(msg, promise)
            return
        }
        try {
            if (msg is ByteBufHolder) {
                writeByteBufHolderMessage(ctx, msg, promise)
            } else {
                writeRegularMessage(ctx, msg, promise)
            }
        } catch (e: EncoderException) {
            throw e
        } catch (t: Throwable) {
            throw EncoderException(t)
        }
    }

    private fun writeRegularMessage(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        promise: ChannelPromise,
    ) {
        var buf: ByteBuf? = null
        try {
            try {
                buf = allocateBuffer(ctx, msg)
                encode(ctx, msg, buf)
            } finally {
                ReferenceCountUtil.release(msg)
            }
            if (buf != null) {
                if (buf.isReadable) {
                    ctx.write(buf, promise)
                } else {
                    buf.release()
                    ctx.write(Unpooled.EMPTY_BUFFER, promise)
                }
            }
            buf = null
        } finally {
            buf?.release()
        }
    }

    private fun <T> writeByteBufHolderMessage(
        ctx: ChannelHandlerContext,
        msg: T,
        promise: ChannelPromise,
    ) where T : OutgoingMessage, T : ByteBufHolder {
        writePacketHeader(ctx, msg)

        val bufHolderContent = msg.content().slice()
        // If there are trailing bytes, we need to encode and write those as well
        if (msg is ByteBufHolderWrapperFooterMessage) {
            // If the byte buf holder wrapper is a footer, use void promise as we don't
            // want to complete the promise until the last bytes of this packet have been written,
            // which would be represented by the footer
            if (bufHolderContent.isReadable) {
                ctx.write(bufHolderContent, ctx.voidPromise())
            } else {
                bufHolderContent.release()
                ctx.write(Unpooled.EMPTY_BUFFER, ctx.voidPromise())
            }

            var footer: ByteBuf? = null
            try {
                footer = allocateBuffer(ctx, msg.nonByteBufHolderSize())
                encodePayload(msg, footer)
                if (footer.isReadable) {
                    ctx.write(footer, promise)
                } else {
                    footer.release()
                    ctx.write(Unpooled.EMPTY_BUFFER, promise)
                }
                footer = null
            } finally {
                footer?.release()
            }
        } else {
            if (bufHolderContent.isReadable) {
                ctx.write(bufHolderContent, promise)
            } else {
                bufHolderContent.release()
                ctx.write(Unpooled.EMPTY_BUFFER, promise)
            }
        }
    }

    private fun <T> writePacketHeader(
        ctx: ChannelHandlerContext,
        msg: T,
    ) where T : OutgoingMessage, T : ByteBufHolder {
        val encoder = repository.getEncoder(msg::class.java)
        val prot = encoder.prot
        val opcode = prot.opcode
        var headerSize = if (opcode >= 0x80) Short.SIZE_BYTES else Byte.SIZE_BYTES
        when (prot.size) {
            Prot.VAR_BYTE -> headerSize += Byte.SIZE_BYTES
            Prot.VAR_SHORT -> headerSize += Short.SIZE_BYTES
        }
        if (msg is ByteBufHolderWrapperHeaderMessage) {
            headerSize += msg.nonByteBufHolderSize()
        }
        var buf: ByteBuf? = null
        try {
            buf = ctx.alloc().ioBuffer(headerSize)
            pSmart1Or2Enc(buf, opcode)
            var bytes = msg.content().readableBytes()
            if (msg is ByteBufHolderWrapperHeaderMessage) {
                bytes += msg.nonByteBufHolderSize()
            }
            if (msg is ByteBufHolderWrapperFooterMessage) {
                bytes += msg.nonByteBufHolderSize()
            }
            when (prot.size) {
                Prot.VAR_BYTE -> buf.p1(bytes)
                Prot.VAR_SHORT -> buf.p2(bytes)
            }
            if (msg is ByteBufHolderWrapperHeaderMessage) {
                encodePayload(msg, buf)
            }
            ctx.write(buf, ctx.voidPromise())
            buf = null
            onMessageWritten(ctx, opcode, bytes)
        } finally {
            buf?.release()
        }
    }

    private fun encodePayload(
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        val encoder = repository.getEncoder(msg::class.java)
        encoder.encode(
            cipher,
            out.toJagByteBuf(),
            msg,
        )
    }

    protected open fun encode(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        val startMarker = out.writerIndex()
        val encoder = repository.getEncoder(msg::class.java)
        val prot = encoder.prot
        val opcode = prot.opcode
        if (encoder.encryptedPayload) {
            pSmart1Or2Enc(out, opcode)
        } else {
            // Write a temporary value for the opcode first
            // We cannot immediately write the real stream-cipher modified opcode
            // as that alters the stream cipher's own state
            if (opcode < 0x80) {
                out.p1(0)
            } else {
                out.p2(0)
            }
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
            cipher,
            out.toJagByteBuf(),
            msg,
        )

        val endMarker = out.writerIndex()
        // Update the size based on the number of bytes written, if it's a var-* packet
        if (sizeMarker != -1) {
            var length = endMarker - payloadMarker

            // Ok login response is a relatively special case that requires encoding the size
            // as either 3 or 4 bytes bigger than it actually is.
            // This is because it is intended to include the header of the first packet that
            // will come after the login response, so the client knows how many bytes to expect
            // right away with the login response. This _could've_ been done differently by
            // Jagex, but it isn't, resulting in this slightly awkward code.
            // If the opcode is > 127, two bytes are needed to encode the opcode,
            // otherwise a single byte is needed. In either case, 2 more bytes are needed
            // for the size of the rebuild login packet itself.
            if (msg is LoginResponse.Ok) {
                length +=
                    if (opcode > MAX_OPCODE_VALUE_FOR_SINGLE_BYTE_OPCODE) {
                        Short.SIZE_BYTES + Short.SIZE_BYTES
                    } else {
                        Byte.SIZE_BYTES + Short.SIZE_BYTES
                    }
            }
            out.writerIndex(sizeMarker)
            when (prot.size) {
                Prot.VAR_BYTE -> {
                    if (validate) {
                        if (length !in 0..MAX_UBYTE_PAYLOAD_SIZE) {
                            out.writerIndex(startMarker)
                            logger.warn {
                                "Server prot $prot length out of bounds; " +
                                    "expected 0..255, received $length; message: $msg"
                            }
                            return
                        }
                    }
                    out.p1(length)
                }
                Prot.VAR_SHORT -> {
                    if (validate) {
                        if (length !in 0..MAX_USHORT_PAYLOAD_SIZE) {
                            out.writerIndex(startMarker)
                            logger.warn {
                                "Server prot $prot length out of bounds; " +
                                    "expected 0..40_000, received $length; message: $msg"
                            }
                            return
                        }
                    }
                    out.p2(length)
                }
            }
            out.writerIndex(endMarker)
        } else if (validate) {
            val length = endMarker - payloadMarker
            if (length != prot.size) {
                out.writerIndex(startMarker)
                logger.warn {
                    "Server prot $prot length out of bounds; " +
                        "expected 0..40_000, received $length; message: $msg"
                }
                return
            }
        }
        onMessageWritten(ctx, opcode, endMarker - payloadMarker)
        if (!encoder.encryptedPayload) {
            out.writerIndex(startMarker)
            pSmart1Or2Enc(out, opcode)
            out.writerIndex(endMarker)
        }
    }

    public open fun onMessageWritten(
        ctx: ChannelHandlerContext,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    /**
     * Writes a byte or short for the opcode with all the bytes
     * encrypted using the stream cipher provided.
     * The name of this function is from a leak.
     */
    private fun pSmart1Or2Enc(
        out: ByteBuf,
        opcode: Int,
    ) {
        if (opcode < 0x80) {
            out.p1(opcode + cipher.nextInt())
        } else {
            out.p1((opcode ushr 8 or 0x80) + cipher.nextInt())
            out.p1((opcode and 0xFF) + cipher.nextInt())
        }
    }

    protected fun allocateBuffer(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
    ): ByteBuf {
        val size = estimator.newHandle().size(msg)
        return ctx.alloc().ioBuffer(size)
    }

    private fun allocateBuffer(
        ctx: ChannelHandlerContext,
        cap: Int,
    ): ByteBuf {
        return ctx.alloc().ioBuffer(cap)
    }

    private companion object {
        /**
         * The highest possible value for an opcode that can still be written in a single byte
         * using a 1 or 2 byte smart.
         */
        private const val MAX_OPCODE_VALUE_FOR_SINGLE_BYTE_OPCODE: Int = 127
        private const val MAX_USHORT_PAYLOAD_SIZE: Int = 40_000
        private const val MAX_UBYTE_PAYLOAD_SIZE: Int = 255
        private val logger = InlineLogger()
    }
}
