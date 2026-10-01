package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.logging.js5Log
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.channel.hostAddress
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.incoming.PriorityChangeHigh
import net.rsprot.protocol.js5.incoming.PriorityChangeLow
import net.rsprot.protocol.js5.incoming.XorChange
import net.rsprot.protocol.message.IncomingJs5Message

/**
 * A channel handler for the JS5 connections
 */
public class Js5ChannelHandler(
    private val networkService: NetworkService<*>,
) : SimpleChannelInboundHandler<IncomingJs5Message>(IncomingJs5Message::class.java) {
    private lateinit var client: Js5Client
    private val service: Js5Service
        get() = networkService.js5Service

    override fun channelActive(ctx: ChannelHandlerContext) {
        networkService
            .iNetAddressHandlers
            .js5InetAddressTracker
            .register(ctx.hostAddress())
        networkLog(logger) {
            "Js5 channel '${ctx.channel()}' is now active"
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService
            .iNetAddressHandlers
            .js5InetAddressTracker
            .deregister(ctx.hostAddress())
        networkLog(logger) {
            "Js5 channel '${ctx.channel()}' is now inactive"
        }
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        // Instantiate the client when the handler is added, additionally read from the ctx
        client = Js5Client(ctx.read())
        service.onClientConnected(client)
        networkService
            .trafficMonitor
            .js5ChannelTrafficMonitor
            .incrementConnections(ctx.hostAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        service.onClientDisconnected(client)
        networkService
            .trafficMonitor
            .js5ChannelTrafficMonitor
            .decrementConnections(ctx.hostAddress())
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingJs5Message,
    ) {
        // Directly handle all the possible message types in descending order of
        // probability of being sent
        when (msg) {
            is Js5GroupRequest -> {
                js5Log(logger) {
                    "JS5 group request from channel '${ctx.channel()}' received: $msg"
                }
                service.push(client, msg)
            }
            PriorityChangeLow -> {
                js5Log(logger) {
                    "Priority changed to low in channel ${ctx.channel()}"
                }
                client.setLowPriority()
                service.readIfNotFull(client)
                // Furthermore, notify the client as we might've transferred prefetch over
                service.notifyIfNotEmpty(client)
            }
            PriorityChangeHigh -> {
                js5Log(logger) {
                    "Priority changed to high in channel ${ctx.channel()}"
                }
                client.setHighPriority()
                service.readIfNotFull(client)
            }
            is XorChange -> {
                js5Log(logger) {
                    "Encryption key received from channel '${ctx.channel()}': $msg"
                }
                service.use {
                    client.setXorKey(msg.key)
                    service.readIfNotFull(client)
                }
            }
            else -> {
                throw IllegalStateException("Unknown JS5 message: $msg")
            }
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        // Read more from the context if we have space to read, when the read has completed
        service.readIfNotFull(client)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        // If the channel turns writable again, allow the service to continue
        // serving to this client
        if (ctx.channel().isWritable) {
            service.notifyIfNotEmpty(client)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        networkService
            .exceptionHandlers
            .channelExceptionHandler
            .exceptionCaught(ctx, cause)
        networkService
            .trafficMonitor
            .js5ChannelTrafficMonitor
            .addDisconnectionReason(
                ctx.hostAddress(),
                Js5DisconnectionReason.EXCEPTION,
            )
        val channel = ctx.channel()
        if (channel.isOpen) {
            channel.close()
        }
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        // Close the context if the channel goes idle
        if (evt is IdleStateEvent) {
            networkLog(logger) {
                "JS5 channel has gone idle, closing channel ${ctx.channel()}"
            }
            networkService
                .trafficMonitor
                .js5ChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.hostAddress(),
                    Js5DisconnectionReason.IDLE,
                )
            ctx.close()
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
