@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.channel.replace
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.game.GameMessageEncoder
import net.rsprot.protocol.api.game.GameMessageHandler
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginClientType
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import java.util.function.Consumer

/**
 * A response handler for login requests, allowing the server to write either
 * a successful or a failed login response, depending on the server's decision.
 * @property networkService the main network service god object
 * @property ctx the channel handler context to write the response to
 */
public class GameLoginResponseHandler<R>(
    public val networkService: NetworkService<R, *>,
    public val ctx: ChannelHandlerContext,
) {
    /**
     * Writes a successful login response to the client.
     * @param response the login response to write
     * @param loginBlock the login request that the client initially made
     * @param callback the callback that will be triggered after the response
     * has been written to the channel. The callback is always triggered,
     * even if the writing failed. The session in the callback will be null
     * if anything went wrong, meaning the server should free up the reserved
     * index for this player sent in the response and treat it as a failed
     * login. If the session exists, the login was successful and the server
     * is expected to assign a disconnect hook right away, to ensure the server
     * is notified if the connection is lost.
     */
    public fun writeSuccessfulResponse(
        response: LoginResponse.Ok,
        loginBlock: LoginBlock<*>,
        callback: Consumer<Session<R>?>,
    ) {
        val oldSchoolClientType =
            when (loginBlock.clientType) {
                LoginClientType.DESKTOP -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_WINDOWS -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_LINUX -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_MAC -> OldSchoolClientType.DESKTOP
                else -> null
            }
        if (oldSchoolClientType == null || !networkService.isSupported(oldSchoolClientType)) {
            networkLog(logger) {
                "Unsupported client type received from channel " +
                    "'${ctx.channel()}': $oldSchoolClientType, login block: $loginBlock"
            }
            ctx.writeAndFlush(LoginResponse.InvalidLoginPacket)
            callback.accept(null)
            return
        }
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
        // Secondary validation just before we allow the server to log the user in
        if (!accepted) {
            networkLog(logger) {
                "INetAddressValidator rejected game login for channel ${ctx.channel()}"
            }
            ctx
                .writeAndFlush(LoginResponse.TooManyAttempts)
                .addListener(ChannelFutureListener.CLOSE)
            return
        }
        val encodeSeed = loginBlock.seed
        val decodeSeed =
            IntArray(encodeSeed.size) { index ->
                encodeSeed[index] + DECODE_SEED_OFFSET
            }

        val encodingCipher = IsaacRandom(decodeSeed)
        val decodingCipher = IsaacRandom(encodeSeed)
        val channel = ctx.channel()
        channel
            .attr(ChannelAttributes.STREAM_CIPHER_PAIR)
            .set(StreamCipherPair(encodingCipher, decodingCipher))
        channel.attr(ChannelAttributes.HUFFMAN_CODEC)
            .set(networkService.huffmanCodecProvider)

        val writeFuture =
            if (networkService.betaWorld) {
                val encoder =
                    networkService
                        .encoderRepositories
                        .loginMessageDecoderRepository
                        .getEncoder(response::class.java)
                val buffer = ctx.alloc().buffer(37 + 1).toJagByteBuf()
                buffer.p1(37)
                encoder.encode(ctx, buffer, response)
                ctx.writeAndFlush(buffer.buffer)
            } else {
                ctx.writeAndFlush(response)
            }

        writeFuture.addListener(
            ChannelFutureListener { future ->
                if (!future.isSuccess) {
                    networkLog(logger) {
                        "Failed to write a successful game login response to channel " +
                            "'${ctx.channel()}': $loginBlock"
                    }
                    future.channel().pipeline().fireExceptionCaught(future.cause())
                    future.channel().close()
                    callback.accept(null)
                    return@ChannelFutureListener
                }
                val pipeline = ctx.channel().pipeline()

                val session =
                    Session(
                        ctx,
                        networkService
                            .gameMessageHandlers
                            .incomingGameMessageQueueProvider
                            .provide(),
                        networkService
                            .gameMessageHandlers
                            .outgoingGameMessageQueueProvider,
                        networkService
                            .gameMessageHandlers
                            .gameMessageCounterProvider
                            .provide(),
                        networkService
                            .gameMessageConsumerRepositoryProvider
                            .provide()
                            .consumers,
                        loginBlock,
                        networkService
                            .exceptionHandlers
                            .incomingGameMessageConsumerExceptionHandler,
                    )
                pipeline.replace<LoginMessageDecoder>(
                    GameMessageDecoder(
                        networkService,
                        session,
                        decodingCipher,
                        oldSchoolClientType,
                    ),
                )
                pipeline.replace<LoginMessageEncoder>(
                    GameMessageEncoder(networkService, encodingCipher, oldSchoolClientType),
                )
                pipeline.replace<LoginConnectionHandler<R>>(GameMessageHandler(networkService, session))
                networkLog(logger) {
                    "Successful game login from channel '${ctx.channel()}': $loginBlock"
                }
                callback.accept(session)
            },
        )
    }

    /**
     * Writes a failed response to the client. This is _all_ requests that aren't the ok
     * response, even ones where technically it's correct - this is because the client
     * always makes a new connection to re-request the login, nothing is kept open
     * over long periods of time.
     * @param response the response to write to the client - this cannot be the successful
     * response or the proof of work response, as those are handled in a special manner.
     */
    public fun writeFailedResponse(response: LoginResponse) {
        if (response is LoginResponse.ProofOfWork) {
            throw IllegalStateException("Proof of Work is handled at the engine level.")
        }
        if (response is LoginResponse.Successful) {
            throw IllegalStateException("Successful login response is handled at the engine level.")
        }
        networkLog(logger) {
            "Writing failed login response to channel '${ctx.channel()}': $response"
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private companion object {
        /**
         * The offset applied to the decode ISAAC stream cipher seed.
         */
        private const val DECODE_SEED_OFFSET: Int = 50
        private val logger: InlineLogger = InlineLogger()
    }
}
