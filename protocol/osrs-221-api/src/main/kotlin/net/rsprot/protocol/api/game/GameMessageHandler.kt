package net.rsprot.protocol.api.game

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.message.IncomingGameMessage

public class GameMessageHandler<R>(
    private val session: Session<R>,
) : SimpleChannelInboundHandler<IncomingGameMessage>() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingGameMessage,
    ) {
        session.addIncomingMessage(msg)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        if (ctx.channel().isWritable) {
            session.flush()
        }
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        if (evt is IdleStateEvent) {
            ctx.close()
        }
    }
}
