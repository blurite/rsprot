package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.exceptions.InvalidVersionException
import net.rsprot.protocol.loginprot.incoming.GameLogin
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.RemainingBetaArchives
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWork
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.IncomingLoginMessage
import java.text.NumberFormat
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.function.BiFunction

/**
 * The login connection handler, responsible for handling any game connections.
 * @property sessionId the session id that was originally generated and written,
 * expected to receive the same session id back from the client in the login block.
 */
@Suppress("DuplicatedCode")
public class LoginConnectionHandler<R>(
    private val networkService: NetworkService<R>,
    private val sessionId: Long,
) : SimpleChannelInboundHandler<IncomingLoginMessage>(IncomingLoginMessage::class.java) {
    private var loginState: LoginState = LoginState.UNINITIALIZED
    private var loginPacket: IncomingLoginMessage? = null
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
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService
            .iNetAddressHandlers
            .gameInetAddressTracker
            .deregister(ctx.inetAddress())
        networkLog(logger) {
            "Channel is now inactive: ${ctx.channel()}"
        }
        ctx.fireChannelInactive()
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
        // If login block isn't initialized yet, or has already been decoded, do nothing
        val loginPacket = this.loginPacket ?: return
        this.loginPacket = null
        val jagBuffer =
            when (val packet = loginPacket) {
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
            is RemainingBetaArchives -> {
                if (this.loginState != LoginState.AWAITING_BETA_RESPONSE) {
                    ctx.close()
                    return
                }
                decodeLoginPacket(ctx, msg)
            }
            is GameLogin -> {
                if (this.loginState != LoginState.UNINITIALIZED) {
                    ctx.close()
                    return
                }
                this.loginPacket = msg
                requestProofOfWork(ctx)
            }

            is GameReconnect -> {
                this.loginPacket = msg
                continueLogin(ctx)
            }

            is ProofOfWorkReply -> {
                if (loginState != LoginState.REQUESTED_PROOF_OF_WORK) {
                    ctx.close()
                    return
                }
                val pow = this.proofOfWork
                verifyProofOfWork(pow, msg.result).handle { success, exception ->
                    try {
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
                        continueLogin(ctx)
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Error in handling processed proof of work."
                        }
                    } catch (t: Throwable) {
                        logger.error(t) {
                            "Fatal error in handling processed proof of work."
                        }
                        throw t
                    }
                }
            }
            else -> {
                throw IllegalStateException("Unknown login connection handler")
            }
        }
    }

    private fun requestProofOfWork(ctx: ChannelHandlerContext) {
        val pow =
            networkService
                .loginHandlers
                .proofOfWorkProvider
                .provide(ctx.inetAddress())
                ?: return continueLogin(ctx)
        loginState = LoginState.REQUESTED_PROOF_OF_WORK
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

    private fun continueLogin(ctx: ChannelHandlerContext) {
        if (networkService.betaWorld) {
            loginState = LoginState.AWAITING_BETA_RESPONSE
            // Instantly request the remaining beta archives, as that feature
            // is implemented incorrectly and serves no functional purpose
            ctx
                .writeAndFlush(ctx.alloc().buffer(1).writeByte(2))
                .addListener(
                    ChannelFutureListener { future ->
                        if (!future.isSuccess) {
                            networkLog(logger) {
                                "Failed to write beta crc request to channel ${ctx.channel()}"
                            }
                            future.channel().pipeline().fireExceptionCaught(future.cause())
                            future.channel().close()
                            return@ChannelFutureListener
                        }
                        ctx.read()
                    },
                )
        } else {
            decodeLoginPacket(ctx, null)
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

    private fun decodeLoginPacket(
        ctx: ChannelHandlerContext,
        remainingBetaArchives: RemainingBetaArchives?,
    ) {
        val loginPacket = this.loginPacket ?: return
        this.loginPacket = null
        val responseHandler = GameLoginResponseHandler(networkService, ctx)
        when (val packet = loginPacket) {
            is GameLogin -> {
                decodeGameLoginBuffer(packet, ctx, remainingBetaArchives, responseHandler)
            }

            is GameReconnect -> {
                decodeGameReconnectBuffer(packet, ctx, remainingBetaArchives, responseHandler)
            }

            else -> {
                throw IllegalStateException("Unknown login packet: $packet")
            }
        }
    }

    private fun decodeGameLoginBuffer(
        packet: GameLogin,
        ctx: ChannelHandlerContext,
        remainingBetaArchives: RemainingBetaArchives?,
        responseHandler: GameLoginResponseHandler<R>,
    ) {
        decodeLogin(
            packet.buffer,
            networkService.betaWorld,
            packet.decoder,
        ).handle { block, exception ->
            try {
                if (block == null || exception != null) {
                    if (exception is CompletionException && exception.cause == InvalidVersionException) {
                        // Write a message indicating client is outdated
                        ctx
                            .writeAndFlush(LoginResponse.ClientOutOfDate)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
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
                if (remainingBetaArchives != null) {
                    block.mergeBetaCrcs(remainingBetaArchives)
                }
                networkLog(logger) {
                    "Successful game login from channel '${ctx.channel()}': $block"
                }
                val executor = networkService.loginHandlers.loginFlowExecutor
                if (executor != null) {
                    executor.submit {
                        try {
                            networkService.gameConnectionHandler.onLogin(responseHandler, block)
                        } catch (t: Throwable) {
                            exceptionCaught(ctx, t)
                        }
                    }
                } else {
                    networkService.gameConnectionHandler.onLogin(responseHandler, block)
                }
            } catch (e: Exception) {
                logger.error(e) {
                    "Error in handling decoded login block."
                }
            } catch (t: Throwable) {
                logger.error(t) {
                    "Fatal error in handling decoded login block."
                }
                throw t
            }
        }
    }

    private fun decodeGameReconnectBuffer(
        packet: GameReconnect,
        ctx: ChannelHandlerContext,
        remainingBetaArchives: RemainingBetaArchives?,
        responseHandler: GameLoginResponseHandler<R>,
    ) {
        decodeLogin(
            packet.buffer,
            networkService.betaWorld,
            packet.decoder,
        ).handle { block, exception ->
            try {
                if (block == null || exception != null) {
                    if (exception is CompletionException && exception.cause == InvalidVersionException) {
                        // Write a message indicating client is outdated
                        ctx
                            .writeAndFlush(LoginResponse.ClientOutOfDate)
                            .addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
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
                if (remainingBetaArchives != null) {
                    block.mergeBetaCrcs(remainingBetaArchives)
                }
                networkLog(logger) {
                    "Successful game reconnection from channel '${ctx.channel()}': $block"
                }
                val executor = networkService.loginHandlers.loginFlowExecutor
                if (executor != null) {
                    executor.submit {
                        try {
                            networkService.gameConnectionHandler.onReconnect(responseHandler, block)
                        } catch (t: Throwable) {
                            exceptionCaught(ctx, t)
                        }
                    }
                } else {
                    networkService.gameConnectionHandler.onReconnect(responseHandler, block)
                }
            } catch (e: Exception) {
                logger.error(e) {
                    "Error in handling decoded login block."
                }
            } catch (t: Throwable) {
                logger.error(t) {
                    "Fatal error in handling decoded login block."
                }
                throw t
            }
        }
    }

    private fun <Buf, Fun> decodeLogin(
        buf: Buf,
        betaWorld: Boolean,
        function: BiFunction<Buf, Boolean, Fun>,
    ): CompletableFuture<Fun> =
        networkService
            .loginHandlers
            .loginDecoderService
            .decode(buf, betaWorld, function)

    private fun <T : ChallengeType<MetaData>, MetaData : ChallengeMetaData> verifyProofOfWork(
        pow: ProofOfWork<T, MetaData>,
        result: Long,
    ): CompletableFuture<Boolean> =
        networkService
            .loginHandlers
            .proofOfWorkChallengeWorker
            .verify(
                result,
                pow.challengeType,
                pow.challengeVerifier,
            )

    private enum class LoginState {
        UNINITIALIZED,
        REQUESTED_PROOF_OF_WORK,
        AWAITING_BETA_RESPONSE,
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
