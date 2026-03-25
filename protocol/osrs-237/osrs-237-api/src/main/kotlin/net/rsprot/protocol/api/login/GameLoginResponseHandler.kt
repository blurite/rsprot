@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.handler.timeout.IdleStateHandler
import net.rsprot.buffer.extensions.gdata
import net.rsprot.buffer.extensions.p8
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.protocol.Prot
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.game.GameMessageEncoder
import net.rsprot.protocol.api.game.GameMessageHandler
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.binary.BinaryBlob
import net.rsprot.protocol.binary.BinaryStream
import net.rsprot.protocol.channel.binaryHeaderBuilderOrNull
import net.rsprot.protocol.channel.hostAddress
import net.rsprot.protocol.channel.replace
import net.rsprot.protocol.channel.setBinaryBlob
import net.rsprot.protocol.channel.setBinaryHeaderBuilder
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import java.security.MessageDigest

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
        val address = ctx.hostAddress()
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
        finalizeBinaryHeader(ctx.channel(), response)
        val session =
            createSession(loginBlock, pipeline, cipher.decodeCipher, oldSchoolClientType, cipher.encoderCipher)
        networkService.js5Authorizer.authorize(ctx.hostAddress())
        ctx.executor().submit {
            ctx.write(buffer.buffer)
            session.onLoginTransitionComplete()
        }
        networkLog(logger) {
            "Successful game login from channel '${ctx.channel()}': $loginBlock"
        }
        return session
    }

    private fun finalizeBinaryHeader(
        channel: Channel,
        response: LoginResponse.Ok,
    ) {
        val provider = networkService.binaryHeaderProvider ?: return
        val builder = channel.binaryHeaderBuilderOrNull() ?: return
        channel.setBinaryHeaderBuilder(null)
        val timestamp = System.currentTimeMillis()
        val accountHash = accountHash(response.userId, response.userHash)
        val partialHeader =
            provider.provide(response.index, timestamp, accountHash)
                ?: return
        builder.timestamp(timestamp)
        builder.localPlayerIndex(response.index)
        builder.accountHash(accountHash)
        builder.path(partialHeader.path)
        builder.worldId(partialHeader.worldId)
        builder.worldProperties(partialHeader.worldFlags)
        builder.worldLocation(partialHeader.worldLocation)
        builder.worldHost(partialHeader.worldHost)
        builder.worldActivity(partialHeader.worldActivity)
        builder.clientName(partialHeader.clientName)
        val masterIndex = networkService.js5Service.getMasterIndex()
        builder.js5MasterIndex(masterIndex)
        val header = builder.build()
        channel.setBinaryBlob(
            BinaryBlob(
                header,
                BinaryStream(header.encode(UnpooledByteBufAllocator.DEFAULT)),
            ),
        )
    }

    private fun accountHash(
        userId: Long,
        userHash: Long,
    ): ByteArray {
        val buffer = Unpooled.buffer(Long.SIZE_BYTES + Long.SIZE_BYTES)
        // User id is an incrementing value; As of writing this comment, there are somewhere between
        // 300-400m users, meaning the userId value for any new accounts would be in that range
        // This value is not sensitive in any way, but it is constant.
        buffer.p8(userId)
        // User hash is an actual hash provided by Jagex, unique for a given account regardless of the world.
        // While hash on its own is not useful, there is a potential security concern in how these hashes
        // are generated. As such, we take an extra step and salt it with the user id, then hash the
        // value once more. Due to the function turning 128 bits of data to 256 bits of data,
        // the probability of collisions is extremely thin.
        buffer.p8(userHash)
        val input = ByteArray(buffer.readableBytes())
        buffer.gdata(input)
        // Take the combined byte array and hash it with a SHA-256 hashing function.
        // This effectively ensures no one will be able to reverse the original input values,
        // while still ensuring we can match multiple play sessions to a single user account.
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return messageDigest.digest()
    }

    public fun writeSuccessfulResponse(
        response: LoginResponse.ReconnectOk,
        loginBlock: LoginBlock<*>,
        previousSession: Session<R>,
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
        val oldBlob = previousSession.getBinaryBlobOrNull()
        if (oldBlob != null) {
            this.ctx.channel().setBinaryBlob(oldBlob)
            oldBlob.stream.append(
                serverToClient = true,
                opcode = 0xFF,
                size = Prot.VAR_SHORT,
                payload = buffer.buffer.retainedSlice(start, written),
            )
        }
        val session =
            createSession(loginBlock, pipeline, decodingCipher, oldSchoolClientType, encodingCipher)
        networkService.js5Authorizer.authorize(ctx.hostAddress())
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
            networkService.idleStateHandlerSuppliers.gameSupplier.supply(),
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
                ctx.hostAddress(),
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
                ctx.hostAddress(),
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
