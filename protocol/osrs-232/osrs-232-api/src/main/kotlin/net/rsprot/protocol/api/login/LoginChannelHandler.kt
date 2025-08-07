package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.channel.replace
import net.rsprot.protocol.api.js5.Js5ChannelHandler
import net.rsprot.protocol.api.js5.Js5MessageDecoder
import net.rsprot.protocol.api.js5.Js5MessageEncoder
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.common.RSProtConstants
import net.rsprot.protocol.loginprot.incoming.InitGameConnection
import net.rsprot.protocol.loginprot.incoming.InitJs5RemoteConnection
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.IncomingLoginMessage
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

/**
 * The channel handler for login channels, essentially the very first requests that will
 * come in from the client, pointing to either JS5 or the game.
 */
@Suppress("DuplicatedCode")
public class LoginChannelHandler(
    public val networkService: NetworkService<*>,
) : SimpleChannelInboundHandler<IncomingLoginMessage>(IncomingLoginMessage::class.java) {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        networkService
            .trafficMonitor
            .loginChannelTrafficMonitor
            .incrementConnections(ctx.inetAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        networkService
            .trafficMonitor
            .loginChannelTrafficMonitor
            .decrementConnections(ctx.inetAddress())
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.read()
        networkLog(logger) {
            "Channel is now active: ${ctx.channel()}"
        }
        ctx.fireChannelActive()
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingLoginMessage,
    ) {
        networkLog(logger) {
            "Login channel message in channel '${ctx.channel()}': $msg"
        }
        when (msg) {
            InitGameConnection -> {
                handleInitGameConnection(ctx)
            }
            is InitJs5RemoteConnection -> {
                handleInitJs5RemoteConnection(ctx, msg.revision, msg.seed)
            }
            // TODO: Unknown, SSL web
            else -> {
                networkService
                    .trafficMonitor
                    .loginChannelTrafficMonitor
                    .addDisconnectionReason(
                        ctx.inetAddress(),
                        LoginDisconnectionReason.CHANNEL_UNKNOWN_PACKET,
                    )
                throw IllegalStateException("Unknown login channel message: $msg")
            }
        }
    }

    private fun handleInitGameConnection(ctx: ChannelHandlerContext) {
        val address = ctx.inetAddress()
        val count =
            networkService
                .iNetAddressHandlers
                .gameInetAddressTracker
                .getCount(address)
        val accepted =
            networkService
                .iNetAddressHandlers
                .inetAddressValidator
                .acceptGameConnection(address, count)
        if (!accepted) {
            networkLog(logger) {
                "INetAddressValidator rejected game connection for channel ${ctx.channel()}"
            }
            ctx
                .writeAndFlush(LoginResponse.TooManyAttempts)
                .addListener(ChannelFutureListener.CLOSE)
            networkService
                .trafficMonitor
                .loginChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    LoginDisconnectionReason.CHANNEL_IP_LIMIT,
                )
            return
        }
        val sessionId =
            networkService
                .loginHandlers
                .sessionIdGenerator
                .generate(address)
        networkLog(logger) {
            "Game connection accepted with session id: ${NumberFormat.getNumberInstance().format(sessionId)}"
        }
        ctx
            .writeAndFlush(LoginResponse.Successful(sessionId))
            .addListener(
                ChannelFutureListener { future ->
                    if (!future.isSuccess) {
                        networkLog(logger) {
                            "Failed to write a successful game connection response to channel ${ctx.channel()}"
                        }
                        future.channel().pipeline().fireExceptionCaught(future.cause())
                        future.channel().close()
                        return@ChannelFutureListener
                    }
                    // Extra validation to ensure we don't get any weird scenarios where it's stuck in memory
                    if (ctx.channel().isActive) {
                        networkLog(logger) {
                            "Tracking game INetAddress for channel '${future.channel()}': $address"
                        }
                        networkService
                            .iNetAddressHandlers
                            .gameInetAddressTracker
                            .register(address)
                    }
                    val pipeline = future.channel().pipeline()
                    pipeline.replace<LoginChannelHandler>(LoginConnectionHandler(networkService, sessionId))
                    pipeline.replace<IdleStateHandler>(
                        IdleStateHandler(
                            true,
                            NetworkService.LOGIN_TIMEOUT_SECONDS,
                            NetworkService.LOGIN_TIMEOUT_SECONDS,
                            NetworkService.LOGIN_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS,
                        ),
                    )
                },
            )
    }

    private fun handleInitJs5RemoteConnection(
        ctx: ChannelHandlerContext,
        revision: Int,
        seed: IntArray,
    ) {
        if (revision != RSProtConstants.REVISION) {
            networkLog(logger) {
                "Invalid JS5 revision received from channel '${ctx.channel()}': $revision"
            }
            networkService
                .trafficMonitor
                .loginChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    LoginDisconnectionReason.CHANNEL_OUT_OF_DATE,
                )
            ctx
                .writeAndFlush(LoginResponse.ClientOutOfDate)
                .addListener(ChannelFutureListener.CLOSE)
            return
        }
        val address = ctx.inetAddress()
        val count =
            networkService
                .iNetAddressHandlers
                .js5InetAddressTracker
                .getCount(address)
        val accepted =
            networkService
                .iNetAddressHandlers
                .inetAddressValidator
                .acceptJs5Connection(address, count, seed)
        if (!accepted) {
            networkLog(logger) {
                "INetAddressValidator rejected JS5 connection for channel ${ctx.channel()}"
            }
            ctx
                .writeAndFlush(LoginResponse.IPLimit)
                .addListener(ChannelFutureListener.CLOSE)
            networkService
                .trafficMonitor
                .loginChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    LoginDisconnectionReason.CHANNEL_IP_LIMIT,
                )
            return
        }
        ctx
            .writeAndFlush(LoginResponse.Successful(null))
            .addListener(
                ChannelFutureListener { future ->
                    if (!future.isSuccess) {
                        networkLog(logger) {
                            "Failed to write a successful JS5 connection response to channel ${ctx.channel()}"
                        }
                        future.channel().pipeline().fireExceptionCaught(future.cause())
                        future.channel().close()
                        return@ChannelFutureListener
                    }
                    // Extra validation to ensure we don't get any weird scenarios where it's stuck in memory
                    if (ctx.channel().isActive) {
                        networkLog(logger) {
                            "Tracking JS5 INetAddress for channel '${future.channel()}': $address"
                        }
                        networkService
                            .iNetAddressHandlers
                            .js5InetAddressTracker
                            .register(address)
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
            .loginChannelTrafficMonitor
            .addDisconnectionReason(
                ctx.inetAddress(),
                LoginDisconnectionReason.CHANNEL_EXCEPTION,
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
        if (evt is IdleStateEvent) {
            networkLog(logger) {
                "Login channel has gone idle, closing channel ${ctx.channel()}"
            }
            networkService
                .trafficMonitor
                .loginChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    LoginDisconnectionReason.CHANNEL_IDLE,
                )
            ctx.close()
        }
        ctx.fireUserEventTriggered(evt)
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
