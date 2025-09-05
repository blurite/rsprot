package org.jire.netty.haproxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import io.netty.handler.timeout.IdleStateEvent
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_CHANNEL_INITIALIZER_CHILD_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_IDLE_STATE_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_MESSAGE_DECODER_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_MESSAGE_HANDLER_NAME

/**
 * Detects if the incoming connection is using the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol.
 * If it is, it adds the necessary handlers to the pipeline.
 * If not, it removes itself from the pipeline (and thus supports non-proxied connections simultaneously).
 *
 * This handler should be added before any other regular handlers.
 *
 * This handler is [Sharable] and thus can be added to multiple pipelines.
 */
@Sharable
public class HAProxyDetectionHandler(
    override val childHandler: ChannelInboundHandler,
    private val haproxyMessageHandler: HAProxyMessageHandler,
) : SimpleChannelInboundHandler<ByteBuf>(true),
    HAProxyParentHandler {
    override fun channelActive(ctx: ChannelHandlerContext) {
        // Because auto-read may be disabled, we need to trigger the detection
        ctx.read()

        ctx.fireChannelActive()
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        ctx.fireUserEventTriggered(evt)

        if (evt is IdleStateEvent) {
            logger.debug {
                "Channel has gone idle during HAProxy detection, closing for ${ctx.channel()}"
            }
            ctx.close()
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ByteBuf,
    ) {
        val channel = ctx.channel()

        val firstByte = msg.getUnsignedByte(0)
        logger.trace {
            "First byte from $channel was $firstByte"
        }
        when (firstByte) {
            FIRST_BYTE_V2, FIRST_BYTE_V1 -> handleProxied(ctx, channel)
            else -> handleNonProxied(ctx, channel)
        }

        val retainedMsg = msg.retain()
        channel.eventLoop().execute {
            ctx.fireChannelRead(retainedMsg)

            // Because auto-read may be disabled, we need to trigger the next read
            ctx.read()
        }
    }

    private fun handleProxied(
        ctx: ChannelHandlerContext,
        channel: Channel,
    ) {
        logger.trace {
            "HAProxy protocol detected from $channel"
        }

        val pipeline = ctx.pipeline()
        val name = ctx.name()
        pipeline.addAfter(
            name,
            HAPROXY_MESSAGE_HANDLER_NAME,
            haproxyMessageHandler,
        )
        pipeline.replace(
            name,
            HAPROXY_MESSAGE_DECODER_HANDLER_NAME,
            HAProxyMessageDecoder(),
        )
    }

    private fun handleNonProxied(
        ctx: ChannelHandlerContext,
        channel: Channel,
    ) {
        logger.trace {
            "HAProxy protocol not detected from $channel"
        }

        val pipeline = ctx.pipeline()
        pipeline.remove(HAPROXY_IDLE_STATE_HANDLER_NAME)
        pipeline.replace(
            this@HAProxyDetectionHandler,
            HAPROXY_CHANNEL_INITIALIZER_CHILD_NAME,
            childHandler,
        )
    }

    private companion object {
        private const val FIRST_BYTE_V2: Short = 0x0D
        private const val FIRST_BYTE_V1: Short = 'P'.code.toShort()

        private val logger = InlineLogger()
    }
}
