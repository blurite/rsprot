package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.EncoderException
import io.netty.util.ReferenceCountUtil
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.api.handlers.OutgoingMessageSizeEstimator
import net.rsprot.protocol.common.js5.outgoing.prot.Js5ServerProt
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * A message encoder for JS5 requests.
 */
public class Js5MessageEncoder(
    public val networkService: NetworkService<*>,
) : OutgoingMessageEncoder() {
    override val cipher: StreamCipher = NopStreamCipher
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.js5MessageEncoderRepository
    override val validate: Boolean = false
    override val estimator: OutgoingMessageSizeEstimator = networkService.messageSizeEstimator

    override fun write(
        ctx: ChannelHandlerContext,
        msg: Any,
        promise: ChannelPromise,
    ) {
        var buf: ByteBuf? = null
        try {
            if (msg is Js5GroupResponse) {
                if (msg.key != 0) {
                    // If an encryption key is used, we allocate a new buffer and follow the normal flow
                    buf = allocateBuffer(ctx, msg)
                    try {
                        encode(ctx, msg, buf)
                    } finally {
                        ReferenceCountUtil.release(msg)
                    }
                    if (buf.isReadable) {
                        ctx.write(buf, promise)
                    } else {
                        buf.release()
                        ctx.write(Unpooled.EMPTY_BUFFER, promise)
                    }
                    buf = null
                } else {
                    // If no encryption key is used, we simply pass on the same JS5 byte buffer
                    // instead of needing to copy it to a new buffer
                    buf = msg.content()

                    // We _only_ call the encode function to trigger our logging, the function
                    // itself will not be doing any encoding if key is zero.
                    encode(ctx, msg, buf)

                    if (buf!!.isReadable) {
                        ctx.write(buf, promise)
                    } else {
                        buf.release()
                        ctx.write(Unpooled.EMPTY_BUFFER, promise)
                    }
                    buf = null
                }
            } else {
                ctx.write(msg, promise)
            }
        } catch (e: EncoderException) {
            throw e
        } catch (t: Throwable) {
            throw EncoderException(t)
        } finally {
            buf?.release()
        }
    }

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        // Unlike all the other encoders, JS5 does not use any opcode system
        // It simply just writes the request ids followed by the payload itself.
        val writerIndex = out.writerIndex()
        val encoder = repository.getEncoder(msg::class.java)
        encoder.encode(
            cipher,
            out.toJagByteBuf(),
            msg,
        )
        val writtenBytes = out.writerIndex() - writerIndex
        networkService
            .trafficMonitor
            .js5ChannelTrafficMonitor
            .incrementOutgoingPacketPayload(ctx.inetAddress(), Js5ServerProt.JS5_GROUP_RESPONSE.opcode, writtenBytes)
    }
}
