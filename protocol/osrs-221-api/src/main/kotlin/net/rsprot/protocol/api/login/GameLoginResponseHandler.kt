@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.api.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
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

public class GameLoginResponseHandler<R>(
    public val networkService: NetworkService<R, *>,
    public val ctx: ChannelHandlerContext,
) {
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
                LoginClientType.ANDROID -> OldSchoolClientType.ANDROID
                LoginClientType.ENHANCED_ANDROID -> OldSchoolClientType.ANDROID
                LoginClientType.IOS -> OldSchoolClientType.IOS
                LoginClientType.ENHANCED_IOS -> OldSchoolClientType.IOS
            }
        if (!networkService.isSupported(oldSchoolClientType)) {
            networkLog(logger) {
                "Unsupported client type received from channel " +
                    "'${ctx.channel()}': $oldSchoolClientType, login block: $loginBlock"
            }
            ctx.writeAndFlush(LoginResponse.InvalidLoginPacket)
            callback.accept(null)
            return
        }
        val address = ctx.inetAddress()
        val count = networkService.gameInetAddressTracker.getCount(address)
        val accepted = networkService.inetAddressValidator.acceptGameConnection(address, count)
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
        ctx.writeAndFlush(response).addListener(
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
                        networkService.incomingGameMessageQueueProvider.provide(),
                        networkService.outgoingGameMessageQueueProvider,
                        networkService.gameMessageCounterProvider.provide(),
                        networkService.gameMessageConsumerRepositoryProvider.provide().consumers,
                        loginBlock,
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
        private const val DECODE_SEED_OFFSET: Int = 50
        private val logger: InlineLogger = InlineLogger()
    }
}
