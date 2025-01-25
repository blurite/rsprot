@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.handler.timeout.IdleStateHandler
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.channel.replace
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.game.GameMessageEncoder
import net.rsprot.protocol.api.game.GameMessageHandler
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import java.util.concurrent.TimeUnit

/**
 * A response handler for login requests, allowing the server to write either
 * a successful or a failed login response, depending on the server's decision.
 * @property networkService the main network service god object
 * @property ctx the channel handler context to write the response to
 */
public class GameLoginResponseHandler<R>(
    public val networkService: NetworkService<R>,
    public val ctx: ChannelHandlerContext,
) {
    /**
     * Writes a successful login response to the client.
     * @param response the login response to write
     * @param loginBlock the login request that the client initially made
     * @return a session object if the login was successful, otherwise a null.
     */
    public fun writeSuccessfulResponse(
        response: LoginResponse.Ok,
        loginBlock: LoginBlock<*>,
    ): Session<R>? {
        // Ensure it isn't null - our decoder pre-validates it long before hitting this function,
        // so this exception should never be hit.
        val oldSchoolClientType =
            checkNotNull(loginBlock.clientType.toOldSchoolClientType()) {
                "Login client type cannot be null"
            }
        if (!ctx.channel().isActive) {
            networkLog(logger) {
                "Channel '${ctx.channel()}' has gone inactive; login block: $loginBlock"
            }
            networkService.trafficMonitor.loginChannelTrafficMonitor.addDisconnectionReason(
                ctx.inetAddress(),
                LoginDisconnectionReason.GAME_CHANNEL_INACTIVE,
            )
            return null
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
            networkService.trafficMonitor.loginChannelTrafficMonitor.addDisconnectionReason(
                ctx.inetAddress(),
                LoginDisconnectionReason.GAME_TOO_MANY_ATTEMPTS,
            )
            return null
        }
        val cipher = createStreamCipherPair(loginBlock)

        if (networkService.betaWorld) {
            val encoder =
                networkService
                    .encoderRepositories
                    .loginMessageEncoderRepository
                    .getEncoder(response::class.java)
            val buffer = ctx.alloc().buffer(37 + 1).toJagByteBuf()
            buffer.p1(37)
            encoder.encode(cipher.encoderCipher, buffer, response)
            ctx.writeAndFlush(buffer.buffer)
        } else {
            ctx.writeAndFlush(response)
        }

        val pipeline = ctx.channel().pipeline()

        val session =
            createSession(loginBlock, pipeline, cipher.decodeCipher, oldSchoolClientType, cipher.encoderCipher)
        networkLog(logger) {
            "Successful game login from channel '${ctx.channel()}': $loginBlock"
        }
        return session
    }

    public fun writeSuccessfulResponse(
        response: LoginResponse.ReconnectOk,
        loginBlock: LoginBlock<*>,
    ): Session<R>? {
        // Ensure it isn't null - our decoder pre-validates it long before hitting this function,
        // so this exception should never be hit.
        val oldSchoolClientType =
            checkNotNull(loginBlock.clientType.toOldSchoolClientType()) {
                "Login client type cannot be null"
            }
        if (!ctx.channel().isActive) {
            networkLog(logger) {
                "Channel '${ctx.channel()}' has gone inactive; login block: $loginBlock"
            }
            networkService.trafficMonitor.loginChannelTrafficMonitor.addDisconnectionReason(
                ctx.inetAddress(),
                LoginDisconnectionReason.GAME_CHANNEL_INACTIVE,
            )
            return null
        }
        val (encodingCipher, decodingCipher) = createStreamCipherPair(loginBlock)

        // Unlike in the above case, we kind of have to assume it was successful
        // as the player is already in the game and needs to continue on as normal
        ctx.write(response, ctx.voidPromise())
        val pipeline = ctx.channel().pipeline()

        val session =
            createSession(loginBlock, pipeline, decodingCipher, oldSchoolClientType, encodingCipher)
        networkLog(logger) {
            "Successful game login from channel '${ctx.channel()}': $loginBlock"
        }
        return session
    }

    private fun createStreamCipherPair(loginBlock: LoginBlock<*>): StreamCipherPair {
        val encodeSeed = loginBlock.seed
        val decodeSeed =
            IntArray(encodeSeed.size) { index ->
                encodeSeed[index] + DECODE_SEED_OFFSET
            }
        val provider = networkService.loginHandlers.streamCipherProvider
        val encodingCipher = provider.provide(decodeSeed)
        val decodingCipher = provider.provide(encodeSeed)
        return StreamCipherPair(encodingCipher, decodingCipher)
    }

    private fun createSession(
        loginBlock: LoginBlock<*>,
        pipeline: ChannelPipeline,
        decodingCipher: StreamCipher,
        oldSchoolClientType: OldSchoolClientType,
        encodingCipher: StreamCipher,
    ): Session<R> {
        val gameMessageConsumerRepository =
            networkService
                .gameMessageConsumerRepositoryProvider
                .provide()
        val session =
            Session(
                networkService.trafficMonitor,
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
                gameMessageConsumerRepository
                    .consumers,
                gameMessageConsumerRepository
                    .globalConsumers,
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
        pipeline.replace<IdleStateHandler>(
            IdleStateHandler(
                true,
                NetworkService.GAME_TIMEOUT_SECONDS,
                NetworkService.GAME_TIMEOUT_SECONDS,
                NetworkService.GAME_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
            ),
        )
        return session
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
        if (!ctx.channel().isActive) {
            networkLog(logger) {
                "Channel '${ctx.channel()}' has gone inactive, skipping failed response."
            }
            networkService.trafficMonitor.loginChannelTrafficMonitor.addDisconnectionReason(
                ctx.inetAddress(),
                LoginDisconnectionReason.GAME_CHANNEL_INACTIVE,
            )
            return
        }
        networkLog(logger) {
            "Writing failed login response to channel '${ctx.channel()}': $response"
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        val disconnectReason = LoginDisconnectionReason.responseToReasonMap[response]
        if (disconnectReason != null) {
            networkService.trafficMonitor.loginChannelTrafficMonitor.addDisconnectionReason(
                ctx.inetAddress(),
                disconnectReason,
            )
        }
    }

    private companion object {
        /**
         * The offset applied to the decode ISAAC stream cipher seed.
         */
        private const val DECODE_SEED_OFFSET: Int = 50
        private val logger: InlineLogger = InlineLogger()
    }
}
