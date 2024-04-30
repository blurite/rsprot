package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.api.logging.js5Log
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.incoming.PriorityChangeHigh
import net.rsprot.protocol.js5.incoming.PriorityChangeLow
import net.rsprot.protocol.js5.incoming.XorChange
import net.rsprot.protocol.message.IncomingJs5Message

public class Js5ChannelHandler<T : Js5GroupType>(
    private val networkService: NetworkService<*, T>,
) : SimpleChannelInboundHandler<IncomingJs5Message>(IncomingJs5Message::class.java) {
    private lateinit var client: Js5Client<T>
    private val service: Js5Service<T>
        get() = networkService.js5Service

    override fun channelActive(ctx: ChannelHandlerContext) {
        networkService.js5InetAddressTracker.register(ctx.inetAddress())
        networkLog(logger) {
            "Js5 channel '${ctx.channel()}' is now active"
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService.js5InetAddressTracker.deregister(ctx.inetAddress())
        networkLog(logger) {
            "Js5 channel '${ctx.channel()}' is now inactive"
        }
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        client = Js5Client(ctx.read())
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingJs5Message,
    ) {
        when (msg) {
            is Js5GroupRequest -> {
                js5Log(logger) {
                    "JS5 group request from channel '${ctx.channel()}' received: $msg"
                }
                service.push(client, msg)
            }
            PriorityChangeHigh -> {
                js5Log(logger) {
                    "Priority changed to high in channel ${ctx.channel()}"
                }
                client.setHighPriority()
                service.readIfNotFull(client)
            }
            PriorityChangeLow -> {
                js5Log(logger) {
                    "Priority changed to low in channel ${ctx.channel()}"
                }
                client.setLowPriority()
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
            else -> throw IllegalStateException("Unknown JS5 message: $msg")
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        service.readIfNotFull(client)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        if (ctx.channel().isWritable) {
            service.notifyIfNotEmpty(client)
        }
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        if (evt is IdleStateEvent) {
            networkLog(logger) {
                "JS5 channel has gone idle, closing channel ${ctx.channel()}"
            }
            ctx.close()
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
