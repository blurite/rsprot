package net.rsprot.protocol.api.login

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.channel.replace
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.game.GameMessageEncoder
import net.rsprot.protocol.api.game.GameMessageHandler
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginClientType
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import java.util.function.Consumer

public class GameLoginResponseHandler(
    public val networkService: NetworkService<*, *>,
    public val ctx: ChannelHandlerContext,
) {
    public fun writeSuccessfulResponse(
        response: LoginResponse.Ok,
        loginBlock: LoginBlock<AuthenticationType<*>>,
        callback: Consumer<Boolean>,
    ) {
        ctx.writeAndFlush(response).addListener(
            ChannelFutureListener { future ->
                if (!future.isSuccess) {
                    future.channel().pipeline().fireExceptionCaught(future.cause())
                    future.channel().close()
                    callback.accept(false)
                    return@ChannelFutureListener
                }
                val pipeline = ctx.channel().pipeline()
                val encodeSeed = loginBlock.seed
                val decodeSeed =
                    IntArray(encodeSeed.size) { index ->
                        encodeSeed[index] + DECODE_SEED_OFFSET
                    }

                val encodingCipher = IsaacRandom(decodeSeed)
                val decodingCipher = IsaacRandom(encodeSeed)
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
                val session =
                    Session(
                        ctx,
                        networkService.incomingGameMessageQueueProvider.provide(),
                        networkService.outgoingGameMessageQueueProvider.provide(),
                        networkService.gameMessageCounterProvider.provide(),
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
                pipeline.replace<GameMessageHandler>(GameMessageHandler(session))
                callback.accept(true)
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
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private companion object {
        private const val DECODE_SEED_OFFSET: Int = 50
    }
}