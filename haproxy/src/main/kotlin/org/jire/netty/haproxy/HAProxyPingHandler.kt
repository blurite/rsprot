package org.jire.netty.haproxy

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.SimpleChannelInboundHandler
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_PING_HANDLER_CHILD_NAME

/**
 * Handles simple ping requests using a custom opcode.
 *
 * If the first byte of the incoming data matches the [requestOpcode], it responds with a single byte
 * containing the [responseOpcode].
 * If it does not match, it replaces itself in the pipeline with the provided [childHandler] and forwards
 * the received data to it.
 *
 * This handler is [Sharable] and thus can be added to multiple pipelines.
 *
 * @param childHandler The handler to replace this one with if the incoming data does not match the ping request.
 * @param requestOpcode The opcode to listen for as a ping request.
 * @param responseOpcode The opcode to respond with as a ping response.
 */
@Sharable
public class HAProxyPingHandler
    @JvmOverloads
    public constructor(
        override val childHandler: ChannelInboundHandler,
        private val requestOpcode: Int = DEFAULT_PING_REQUEST_OPCODE,
        private val responseOpcode: Int = DEFAULT_PING_RESPONSE_OPCODE,
    ) : SimpleChannelInboundHandler<ByteBuf>(true),
        HAProxyParentHandler {
        private val response: ByteBuf =
            Unpooled.unreleasableBuffer(
                Unpooled
                    .directBuffer(1, 1)
                    .writeByte(responseOpcode),
            )

        override fun channelRead0(
            ctx: ChannelHandlerContext,
            msg: ByteBuf,
        ) {
            val readerIndex = msg.readerIndex()
            val opcode = msg.getUnsignedByte(readerIndex).toInt()
            if (opcode == requestOpcode) {
                msg.readerIndex(readerIndex + 1)
                ctx.writeAndFlush(response, ctx.voidPromise())
            } else {
                val pipeline = ctx.pipeline()
                pipeline.replace(
                    this,
                    HAPROXY_PING_HANDLER_CHILD_NAME,
                    childHandler,
                )

                val retainedMsg = msg.retain()
                ctx.channel().eventLoop().execute {
                    ctx.fireChannelRead(retainedMsg)

                    // Because auto-read may be disabled, we need to trigger the next read
                    ctx.read()
                }
            }
        }

        public companion object {
            public const val DEFAULT_PING_REQUEST_OPCODE: Int = 200
            public const val DEFAULT_PING_RESPONSE_OPCODE: Int = 201
        }
    }
