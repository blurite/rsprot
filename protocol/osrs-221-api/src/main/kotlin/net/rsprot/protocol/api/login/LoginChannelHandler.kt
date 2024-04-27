package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.replace
import net.rsprot.protocol.api.js5.Js5ChannelHandler
import net.rsprot.protocol.api.js5.Js5MessageDecoder
import net.rsprot.protocol.api.js5.Js5MessageEncoder
import net.rsprot.protocol.loginprot.incoming.InitGameConnection
import net.rsprot.protocol.loginprot.incoming.InitJs5RemoteConnection
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.IncomingLoginMessage
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

public class LoginChannelHandler(
    public val networkService: NetworkService<*, *>,
) : SimpleChannelInboundHandler<IncomingLoginMessage>(IncomingLoginMessage::class.java) {
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingLoginMessage,
    ) {
        logger.debug {
            "Received login channel handler: $msg"
        }
        when (msg) {
            InitGameConnection -> {
                handleInitGameConnection(ctx)
            }
            is InitJs5RemoteConnection -> {
                handleInitJs5RemoteConnection(ctx, msg.revision)
            }
            // TODO: Beta archives, unknown, SSL web
            else -> {
                throw IllegalStateException("Unknown login channel message: $msg")
            }
        }
    }

    private fun handleInitGameConnection(ctx: ChannelHandlerContext) {
        ctx.write(LoginResponse.Successful(ThreadLocalRandom.current().nextLong()))
            .addListener(
                ChannelFutureListener { future ->
                    if (!future.isSuccess) {
                        future.channel().pipeline().fireExceptionCaught(future.cause())
                        future.channel().close()
                        return@ChannelFutureListener
                    }
                    future
                        .channel()
                        .pipeline()
                        .replace<LoginChannelHandler>(LoginConnectionHandler(networkService))
                },
            )
    }

    private fun handleInitJs5RemoteConnection(
        ctx: ChannelHandlerContext,
        revision: Int,
    ) {
        if (revision != NetworkService.REVISION) {
            ctx
                .write(LoginResponse.ClientOutOfDate)
                .addListener(ChannelFutureListener.CLOSE)
            return
        }
        ctx.write(LoginResponse.Successful(null))
            .addListener(
                ChannelFutureListener { future ->
                    if (!future.isSuccess) {
                        future.channel().pipeline().fireExceptionCaught(future.cause())
                        future.channel().close()
                        return@ChannelFutureListener
                    }
                    val pipeline = ctx.channel().pipeline()
                    pipeline.replace<LoginMessageDecoder>(Js5MessageDecoder(networkService))
                    pipeline.replace<LoginMessageEncoder>(Js5MessageEncoder(networkService))
                    pipeline.replace<LoginChannelHandler>(Js5ChannelHandler(networkService))
                    pipeline.replace<IdleStateHandler>(
                        IdleStateHandler(
                            true,
                            NetworkService.JS5_TIMEOUT_SECONDS,
                            NetworkService.JS5_TIMEOUT_SECONDS,
                            NetworkService.JS5_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS,
                        ),
                    )
                },
            )
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        if (evt is IdleStateEvent) {
            ctx.close()
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
