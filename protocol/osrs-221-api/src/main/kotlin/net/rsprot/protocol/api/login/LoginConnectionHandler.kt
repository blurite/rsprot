package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.loginprot.incoming.GameLogin
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWork
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.IncomingLoginMessage
import java.util.concurrent.CompletableFuture
import java.util.function.Function

public class LoginConnectionHandler(
    private val networkService: NetworkService<*, *>,
) : SimpleChannelInboundHandler<IncomingLoginMessage>(IncomingLoginMessage::class.java) {
    private var loginState: LoginState = LoginState.UNINITIALIZED
    private lateinit var loginPacket: IncomingLoginMessage
    private lateinit var proofOfWork: ProofOfWork<*, *>

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: IncomingLoginMessage,
    ) {
        logger.debug {
            "Received login connection: $msg"
        }
        when (msg) {
            is GameLogin, is GameReconnect -> {
                loginState = LoginState.REQUESTED_PROOF_OF_WORK
                loginPacket = msg
                val pow = networkService.proofOfWorkProvider.provide(ctx.inetAddress())
                this.proofOfWork = pow
                ctx.write(LoginResponse.ProofOfWork(pow)).addListener(
                    ChannelFutureListener { future ->
                        if (!future.isSuccess) {
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
                    if (success != true || exception != null) {
                        logger.debug {
                            "Proof of work result was incorrect: $success, $exception"
                        }
                        ctx.write(LoginResponse.LoginFail1).addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    logger.debug {
                        "Decoding login packet!"
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

    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any,
    ) {
        if (evt is IdleStateEvent) {
            ctx.close()
        }
    }

    private fun decodeLoginPacket(ctx: ChannelHandlerContext) {
        val responseHandler = GameLoginResponseHandler(networkService, ctx)
        when (val packet = loginPacket) {
            is GameLogin -> {
                decodeLogin(packet.buffer, packet.decoder).handle { block, exception ->
                    if (block == null || exception != null) {
                        logger.debug {
                            "Failed to decode login block: $block, $exception"
                        }
                        ctx.writeAndFlush(LoginResponse.LoginFail2).addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    logger.debug {
                        "Successfully decoded login block: $block"
                    }
                    networkService.gameLoginHandler.onLogin(responseHandler, block)
                }
            }

            is GameReconnect -> {
                decodeLogin(packet.buffer, packet.decoder).handle { block, exception ->
                    if (block == null || exception != null) {
                        ctx.writeAndFlush(LoginResponse.LoginFail2).addListener(ChannelFutureListener.CLOSE)
                        return@handle
                    }
                    networkService.gameLoginHandler.onReconnect(responseHandler, block)
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
        return networkService.loginDecoderService.decode(buf, function)
    }

    private fun <T : ChallengeType<MetaData>, MetaData : ChallengeMetaData> verifyProofOfWork(
        pow: ProofOfWork<T, MetaData>,
        result: Long,
    ): CompletableFuture<Boolean> {
        return networkService.proofOfWorkChallengeWorker
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