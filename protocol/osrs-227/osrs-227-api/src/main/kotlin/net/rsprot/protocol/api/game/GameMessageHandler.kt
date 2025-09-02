package net.rsprot.protocol.api.game

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * The handler for game messages.
 */
public class GameMessageHandler<R>(
    private val networkService: NetworkService<R>,
    private val session: Session<R>,
) : SimpleChannelInboundHandler<IncomingGameMessage>() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        // As auto-read is false, immediately begin reading once this handler
        // has been added post-login
        ctx.read()
        networkService
            .trafficMonitor
            .gameChannelTrafficMonitor
            .incrementConnections(ctx.inetAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        networkService
            .trafficMonitor
            .gameChannelTrafficMonitor
            .decrementConnections(ctx.inetAddress())
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        // Register this channel in the respective address tracker
        networkService
            .iNetAddressHandlers
            .gameInetAddressTracker
            .register(ctx.inetAddress())
        networkLog(logger) {
            "Channel is now active: ${ctx.channel()}"
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        // Triggers the disconnection hook when the channel goes inactive.
        // This is the earliest guaranteed known point of no return
        try {
            session.triggerIdleClosing()
        } finally {
            ctx.fireChannelInactive()
            val address = ctx.inetAddress()
            networkService.js5Authorizer.unauthorize(address)
            // Must ensure both blocks of code get invoked, even if one throws an exception
            networkService
                .iNetAddressHandlers
                .gameInetAddressTracker
                .deregister(address)
            networkLog(logger) {
                "Channel is now inactive: ${ctx.channel()}"
            }
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingGameMessage,
    ) {
        networkLog(logger) {
            "Incoming game message accepted from channel '${ctx.channel()}': $msg"
        }
        session.addIncomingMessage(msg)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        if (ctx.channel().isWritable) {
            networkLog(logger) {
                "Channel '${ctx.channel()}' is now writable again, continuing to write game packets"
            }
            session.flush()
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
            .gameChannelTrafficMonitor
            .addDisconnectionReason(
                ctx.inetAddress(),
                GameDisconnectionReason.EXCEPTION,
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
        // Handle idle states by disconnecting the user if this is hit. This is normally reached
        // after 60 seconds of idle status.
        if (evt is IdleStateEvent) {
            networkLog(logger) {
                "Login connection has gone idle, closing channel ${ctx.channel()}"
            }
            networkService
                .trafficMonitor
                .gameChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    GameDisconnectionReason.IDLE,
                )
            ctx.close()
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
