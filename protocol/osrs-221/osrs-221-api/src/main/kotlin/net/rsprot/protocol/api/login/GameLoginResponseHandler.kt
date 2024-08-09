@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
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
     * @return a session object if the login was successful, otherwise a null.
     */
    public fun writeSuccessfulResponse(
        response: LoginResponse.Ok,
        loginBlock: LoginBlock<*>,
    ): Session<R>? {
        val oldSchoolClientType =
            getOldSchoolClientType(loginBlock)
        if (oldSchoolClientType == null || !networkService.isSupported(oldSchoolClientType)) {
            networkLog(logger) {
                "Unsupported client type received from channel " +
                    "'${ctx.channel()}': $oldSchoolClientType, login block: $loginBlock"
            }
            ctx.writeAndFlush(LoginResponse.InvalidLoginPacket)
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
            return null
        }
        val (encodingCipher, decodingCipher) = setEncodingPair(loginBlock)

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

        val pipeline = ctx.channel().pipeline()

        val session =
            createSession(loginBlock, pipeline, decodingCipher, oldSchoolClientType, encodingCipher)
        networkLog(logger) {
            "Successful game login from channel '${ctx.channel()}': $loginBlock"
        }
        return session
    }

    public fun writeSuccessfulResponse(
        response: LoginResponse.ReconnectOk,
        loginBlock: LoginBlock<*>,
    ): Session<R>? {
        val oldSchoolClientType =
            getOldSchoolClientType(loginBlock)
        if (oldSchoolClientType == null || !networkService.isSupported(oldSchoolClientType)) {
            networkLog(logger) {
                "Unsupported client type received from channel " +
                    "'${ctx.channel()}': $oldSchoolClientType, login block: $loginBlock"
            }
            ctx.writeAndFlush(LoginResponse.InvalidLoginPacket)
            return null
        }
        val (encodingCipher, decodingCipher) = setEncodingPair(loginBlock)

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

    private fun setEncodingPair(loginBlock: LoginBlock<*>): Pair<IsaacRandom, IsaacRandom> {
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
        return Pair(encodingCipher, decodingCipher)
    }

    private fun getOldSchoolClientType(loginBlock: LoginBlock<*>): OldSchoolClientType? {
        val oldSchoolClientType =
            when (loginBlock.clientType) {
                LoginClientType.DESKTOP -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_WINDOWS -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_LINUX -> OldSchoolClientType.DESKTOP
                LoginClientType.ENHANCED_MAC -> OldSchoolClientType.DESKTOP
                else -> null
            }
        return oldSchoolClientType
    }

    private fun createSession(
        loginBlock: LoginBlock<*>,
        pipeline: ChannelPipeline,
        decodingCipher: IsaacRandom,
        oldSchoolClientType: OldSchoolClientType,
        encodingCipher: IsaacRandom,
    ): Session<R> {
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
