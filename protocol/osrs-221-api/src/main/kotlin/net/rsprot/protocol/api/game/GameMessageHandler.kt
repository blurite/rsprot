package net.rsprot.protocol.api.game

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.message.IncomingGameMessage

public class GameMessageHandler<R>(
    private val networkService: NetworkService<R, *>,
    private val session: Session<R>,
) : SimpleChannelInboundHandler<IncomingGameMessage>() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        networkService.gameInetAddressTracker.register(ctx.inetAddress())
        networkLog(logger) {
            "Channel is now active: ${ctx.channel()}"
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        try {
            session.disconnectionHook?.run()
        } finally {
            // Must ensure both blocks of code get invoked, even if one throws an exception
            networkService.gameInetAddressTracker.deregister(ctx.inetAddress())
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
            .channelExceptionHandler
            .exceptionCaught(ctx, cause)
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        if (evt is IdleStateEvent) {
            networkLog(logger) {
                "Login connection has gone idle, closing channel ${ctx.channel()}"
            }
            ctx.close()
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
