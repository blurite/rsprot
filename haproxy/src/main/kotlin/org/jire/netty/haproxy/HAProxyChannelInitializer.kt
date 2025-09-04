package org.jire.netty.haproxy

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.haproxy.HAProxyMessage
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.jire.netty.haproxy.HAProxyAttributes.sourceAddress
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_CHANNEL_INITIALIZER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_DETECTION_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_IDLE_STATE_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_MESSAGE_DECODER_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyHandlerNames.HAPROXY_MESSAGE_HANDLER_NAME
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT_UNIT
import java.util.concurrent.TimeUnit

/**
 * Initializes the channel pipeline with handlers for handling the
 * [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol.
 *
 * This includes an [IdleStateHandler] to close idle connections, a [HAProxyMessageDecoder] to decode
 * the [HAProxyMessage]s, and a [HAProxyMessageHandler] to handle the decoded messages.
 *
 * @param childInitializer The initializer to use for initializing the child channel after the HAProxy handlers.
 *
 * @param mode The [HAProxyMode] to use. Default is [HAProxyMode.AUTO] which will
 * support both proxied and non-proxied connections.
 *
 * @param idleTimeout The timeout duration after which an idle connection will be closed.
 * Default to [DEFAULT_IDLE_TIMEOUT].
 * @param idleTimeoutUnit The time unit for the [idleTimeout].
 * Default to [DEFAULT_IDLE_TIMEOUT_UNIT].
 */
@Sharable
public class HAProxyChannelInitializer<C : Channel>
    @JvmOverloads
    public constructor(
        private val childInitializer: ChannelInitializer<C>,
        private val mode: HAProxyMode = HAProxyMode.AUTO,
        private val idleTimeout: Long = DEFAULT_IDLE_TIMEOUT,
        private val idleTimeoutUnit: TimeUnit = DEFAULT_IDLE_TIMEOUT_UNIT,
    ) : ChannelInitializer<C>() {
        private val messageHandler: HAProxyMessageHandler<C> =
            HAProxyMessageHandler(childInitializer)

        private val detectionHandler: HAProxyDetectionHandler<C> =
            HAProxyDetectionHandler(childInitializer, messageHandler)

        override fun channelActive(ctx: ChannelHandlerContext) {
            // Because auto-read may be disabled, we need to trigger the detection
            ctx.read()

            ctx.fireChannelActive()
        }

        override fun initChannel(ch: C) {
            val pipeline = ch.pipeline()

            when (mode) {
                HAProxyMode.AUTO -> {
                    addIdleStateHandler(pipeline)
                    pipeline.addLast(
                        HAPROXY_DETECTION_HANDLER_NAME,
                        detectionHandler,
                    )
                }

                HAProxyMode.ON -> {
                    addIdleStateHandler(pipeline)
                    addHAProxyHandlers(pipeline)
                }

                HAProxyMode.OFF -> {
                    pipeline.replace(
                        this@HAProxyChannelInitializer,
                        HAPROXY_CHANNEL_INITIALIZER_NAME,
                        childInitializer,
                    )
                }
            }
        }

        override fun userEventTriggered(
            ctx: ChannelHandlerContext,
            evt: Any,
        ) {
            if (evt is IdleStateEvent) {
                logger.debug {
                    "Channel has gone idle, closing for ${ctx.sourceAddress}"
                }
                ctx.close()
                return
            }

            childInitializer.userEventTriggered(ctx, evt)
        }

        public fun addIdleStateHandler(pipeline: ChannelPipeline) {
            pipeline.addLast(
                HAPROXY_IDLE_STATE_HANDLER_NAME,
                HAProxyIdleStateHandler(
                    idleTimeout,
                    idleTimeoutUnit,
                ),
            )
        }

        public fun addHAProxyHandlers(pipeline: ChannelPipeline) {
            pipeline.addLast(
                HAPROXY_MESSAGE_DECODER_HANDLER_NAME,
                HAProxyMessageDecoder(),
            )
            pipeline.addLast(
                HAPROXY_MESSAGE_HANDLER_NAME,
                messageHandler,
            )
        }

        private companion object {
            private val logger = InlineLogger()
        }
    }
