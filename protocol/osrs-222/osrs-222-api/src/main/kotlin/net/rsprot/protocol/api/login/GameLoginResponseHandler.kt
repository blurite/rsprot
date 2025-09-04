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
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.game.GameMessageEncoder
import net.rsprot.protocol.api.game.GameMessageHandler
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.channel.replace
import net.rsprot.protocol.channel.socketAddress
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
     * Validates the new connection by ensuring the connected user hasn't reached an IP limitation due
     * to too many connections from the same IP.
     * This function does not write any response to the client. It simply returns whether a new connection
     * is allowed to take place. The server is responsible for writing the [LoginResponse.TooManyAttempts]
     * response back to the client via [writeFailedResponse], should they wish to do so.
     */
    public fun validateNewConnection(): Boolean {
        val address = ctx.socketAddress()
        val count =
            networkService
                .iNetAddressHandlers
                .gameInetAddressTracker
                .getCount(address)
        return networkService
            .iNetAddressHandlers
            .inetAddressValidator
            .acceptGameConnection(address, count)
    }

    /**
     * Writes a successful login response to the client.
     * @param response the login response to write
     * @param loginBlock the login request that the client initially made
     * @return a session object regardless of if the connection is still alive. If the connection has died,
     * the disconnection hook will be triggered immediately upon being assigned.
     */
    public fun writeSuccessfulResponse(
        response: LoginResponse.Ok,
        loginBlock: LoginBlock<*>,
    ): Session<R> {
        // Ensure it isn't null - our decoder pre-validates it long before hitting this function,
        // so this exception should never be hit.
        val oldSchoolClientType =
            checkNotNull(loginBlock.clientType.toOldSchoolClientType()) {
                "Login client type cannot be null"
            }
        val cipher = createStreamCipherPair(loginBlock)
        val encoder =
            networkService
                .encoderRepositories
                .loginMessageEncoderRepository
                .getEncoder(response::class.java)

        // Special logic here due to beta worlds having a special login flow, and Netty 4.2.RC3 doing
        // a breaking change which gave each pipeline handler its own executor. The fact the executors
        // are no longer the same requires us to delicately write the data out in a predictable manner.

        // See: https://github.com/netty/netty/pull/14705
        // > This also means that some code now moves from the executor of the target context,
        // > to the executor of the calling context. This can create different behaviors from Netty 4.1,
        // > if the pipeline has multiple handlers, is modified by the handlers during the call,
        // > and the handlers use child-executors.

        // This issue was experienced in production by having LoginResponse.Ok arrive after certain
        // game packets, due to the executor differing and race conditions taking place.

        val buffer = ctx.alloc().buffer(37).toJagByteBuf()
        if (!networkService.betaWorld) {
            buffer.p1(encoder.prot.opcode)
        }
        // Client expects a hardcoded 37 value for the size, even though it is not the exact size
        // of the login packet
        buffer.p1(37)
        encoder.encode(cipher.encoderCipher, buffer, response)

        val pipeline = ctx.channel().pipeline()

        val session =
            createSession(loginBlock, pipeline, cipher.decodeCipher, oldSchoolClientType, cipher.encoderCipher)
        networkService.js5Authorizer.authorize(ctx.socketAddress())
        ctx.executor().submit {
            ctx.write(buffer.buffer)
            session.onLoginTransitionComplete()
        }
        networkLog(logger) {
            "Successful game login from channel '${ctx.channel()}': $loginBlock"
        }
        return session
    }

    public fun writeSuccessfulResponse(
        response: LoginResponse.ReconnectOk,
        loginBlock: LoginBlock<*>,
    ): Session<R> {
        // Ensure it isn't null - our decoder pre-validates it long before hitting this function,
        // so this exception should never be hit.
        val oldSchoolClientType =
            checkNotNull(loginBlock.clientType.toOldSchoolClientType()) {
                "Login client type cannot be null"
            }
        val (encodingCipher, decodingCipher) = createStreamCipherPair(loginBlock)

        val encoder =
            networkService
                .encoderRepositories
                .loginMessageEncoderRepository
                .getEncoder(response::class.java)

        // Allocate a perfectly-sized buffer for this packet
        val bufLength = Byte.SIZE_BYTES + Short.SIZE_BYTES + response.content().readableBytes()
        val buffer = ctx.alloc().buffer(bufLength).toJagByteBuf()
        buffer.p1(encoder.prot.opcode)

        // Write a placeholder size of 0 bytes
        val lengthPos = buffer.writerIndex()
        buffer.p2(0)

        // Write the payload
        val start = buffer.writerIndex()
        encoder.encode(encodingCipher, buffer, response)
        val end = buffer.writerIndex()
        val written = end - start

        // Update the size with the actual number of bytes written
        buffer.writerIndex(lengthPos)
        buffer.p2(written)
        buffer.writerIndex(end)

        val pipeline = ctx.channel().pipeline()

        val session =
            createSession(loginBlock, pipeline, decodingCipher, oldSchoolClientType, encodingCipher)
        networkService.js5Authorizer.authorize(ctx.socketAddress())
        ctx.executor().submit {
            ctx.write(buffer.buffer)
            session.onLoginTransitionComplete()
        }
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
            return
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
