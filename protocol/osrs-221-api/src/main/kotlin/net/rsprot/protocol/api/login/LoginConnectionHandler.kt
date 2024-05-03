package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.loginprot.incoming.GameLogin
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWork
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.IncomingLoginMessage
import java.text.NumberFormat
import java.util.concurrent.CompletableFuture
import java.util.function.Function

@Suppress("DuplicatedCode")
public class LoginConnectionHandler<R>(
    private val networkService: NetworkService<R, *>,
    private val sessionId: Long,
) : SimpleChannelInboundHandler<IncomingLoginMessage>(IncomingLoginMessage::class.java) {
    private var loginState: LoginState = LoginState.UNINITIALIZED
    private lateinit var loginPacket: IncomingLoginMessage
    private lateinit var proofOfWork: ProofOfWork<*, *>

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        networkService
            .iNetAddressHandlers
            .gameInetAddressTracker
            .register(ctx.inetAddress())
        networkLog(logger) {
            "Channel is now active: ${ctx.channel()}"
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService
            .iNetAddressHandlers
            .gameInetAddressTracker
            .deregister(ctx.inetAddress())
        networkLog(logger) {
            "Channel is now inactive: ${ctx.channel()}"
        }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        // If the channel is unregistered, we must release the login block buffer
        if (this.loginState == LoginState.REQUESTED_PROOF_OF_WORK) {
            releaseLoginBlock()
        }
    }

    /**
     * Release the login block buffer that was supposed to be decoded after a successful
     * proof of work response.
     */
    private fun releaseLoginBlock() {
        // If login block isn't initialized yet, do nothing
        if (!this::loginPacket.isInitialized) {
            return
        }
        val jagBuffer =
            when (val packet = this.loginPacket) {
                is GameLogin -> packet.buffer
                is GameReconnect -> packet.buffer
                else -> return
            }
        val buffer = jagBuffer.buffer
        val refCnt = buffer.refCnt()
        if (refCnt > 0) {
            buffer.release(refCnt)
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingLoginMessage,
    ) {
        networkLog(logger) {
            "Login connection message in channel '${ctx.channel()}': $msg"
        }
        when (msg) {
            is GameLogin, is GameReconnect -> {
                if (this.loginState != LoginState.UNINITIALIZED) {
                    ctx.close()
                    return
                }
                loginState = LoginState.REQUESTED_PROOF_OF_WORK
                loginPacket = msg
                val pow =
                    networkService
                        .loginHandlers
                        .proofOfWorkProvider
                        .provide(ctx.inetAddress())
                this.proofOfWork = pow
                ctx.writeAndFlush(LoginResponse.ProofOfWork(pow)).addListener(
                    ChannelFutureListener { future ->
                        if (!future.isSuccess) {
                            networkLog(logger) {
                                "Failed to write a successful proof of work request to channel ${ctx.channel()}"
                            }
                            future.channel().pipeline().fireExceptionCaught(future.cause())
                            future.channel().close()
                            return@ChannelFutureListener
                        }
                        ctx.read()
                    },
                )
            }

            is ProofOfWorkReply -> {
                if (loginState != LoginState.REQUESTED_PROOF_OF_WORK) {
                    ctx.close()
                    return
                }
                val pow = this.proofOfWork
                verifyProofOfWork(pow, msg.result).handle { success, exception ->
                    if (success != true) {
                        networkLog(logger) {
                            "Incorrect proof of work response received from " +
                                "channel '${ctx.channel()}': ${msg.result}, challenge was: $pow"
                        }
                        ctx.writeAndFlush(LoginResponse.LoginFail1).addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    if (exception != null) {
                        logger.error(exception) {
                            "Exception during proof of work verification " +
                                "from channel '${ctx.channel()}': $exception"
                        }
                        ctx.writeAndFlush(LoginResponse.LoginFail1).addListener(ChannelFutureListener.CLOSE)
                    }
                    networkLog(logger) {
                        "Correct proof of work response received from channel '${ctx.channel()}': ${msg.result}"
                    }
                    decodeLoginPacket(ctx)
                }
            }
            else -> {
                throw IllegalStateException("Unknown login connection handler")
            }
        }
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

    private fun decodeLoginPacket(ctx: ChannelHandlerContext) {
        val responseHandler = GameLoginResponseHandler(networkService, ctx)
        when (val packet = loginPacket) {
            is GameLogin -> {
                decodeLogin(packet.buffer, packet.decoder).handle { block, exception ->
                    if (block == null || exception != null) {
                        logger.error(exception) {
                            "Failed to decode game login block for channel ${ctx.channel()}"
                        }
                        ctx
                            .writeAndFlush(LoginResponse.LoginFail2)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    if (sessionId != block.sessionId) {
                        networkLog(logger) {
                            "Mismatching game login session id received from channel " +
                                "'${ctx.channel()}': ${NumberFormat.getNumberInstance().format(block.sessionId)}, " +
                                "expected value: ${NumberFormat.getNumberInstance().format(sessionId)}"
                        }
                        ctx
                            .writeAndFlush(LoginResponse.InvalidLoginPacket)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    networkLog(logger) {
                        "Successful game login from channel '${ctx.channel()}': $block"
                    }
                    networkService.gameConnectionHandler.onLogin(responseHandler, block)
                }
            }

            is GameReconnect -> {
                decodeLogin(packet.buffer, packet.decoder).handle { block, exception ->
                    if (block == null || exception != null) {
                        logger.error(exception) {
                            "Failed to decode game reconnect block for channel ${ctx.channel()}"
                        }
                        ctx
                            .writeAndFlush(LoginResponse.LoginFail2)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    if (sessionId != block.sessionId) {
                        networkLog(logger) {
                            "Mismatching reconnect session id received from channel " +
                                "'${ctx.channel()}': ${NumberFormat.getNumberInstance().format(block.sessionId)}, " +
                                "expected value: ${NumberFormat.getNumberInstance().format(sessionId)}"
                        }
                        ctx
                            .writeAndFlush(LoginResponse.InvalidLoginPacket)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    networkLog(logger) {
                        "Successful game reconnection from channel '${ctx.channel()}': $block"
                    }
                    networkService.gameConnectionHandler.onReconnect(responseHandler, block)
                }
            }

            else -> {
                throw IllegalStateException("Unknown login packet: $packet")
            }
        }
    }

    private fun <Buf, Fun> decodeLogin(
        buf: Buf,
        function: Function<Buf, Fun>,
    ): CompletableFuture<Fun> {
        return networkService
            .loginHandlers
            .loginDecoderService
            .decode(buf, function)
    }

    private fun <T : ChallengeType<MetaData>, MetaData : ChallengeMetaData> verifyProofOfWork(
        pow: ProofOfWork<T, MetaData>,
        result: Long,
    ): CompletableFuture<Boolean> {
        return networkService
            .loginHandlers
            .proofOfWorkChallengeWorker
            .verify(
                result,
                pow.challengeType,
                pow.challengeVerifier,
            )
    }

    private enum class LoginState {
        UNINITIALIZED,
        REQUESTED_PROOF_OF_WORK,
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
