package net.rsprot.protocol.api

import io.netty.channel.ChannelFuture
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.api.bootstrap.BootstrapFactory
import net.rsprot.protocol.api.implementation.DefaultGameMessageCounterProvider
import net.rsprot.protocol.api.implementation.DefaultInetAddressValidator
import net.rsprot.protocol.api.implementation.DefaultLoginDecoderService
import net.rsprot.protocol.api.implementation.DefaultMessageQueueProvider
import net.rsprot.protocol.api.implementation.DefaultStreamCipherProvider
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.api.js5.Js5Service
import net.rsprot.protocol.api.repositories.MessageDecoderRepositories
import net.rsprot.protocol.api.repositories.MessageEncoderRepositories
import net.rsprot.protocol.api.util.asCompletableFuture
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWorkProvider
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeWorker
import net.rsprot.protocol.loginprot.incoming.pow.challenges.DefaultChallengeWorker
import net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.DefaultSha256ProofOfWorkProvider
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository
import net.rsprot.protocol.tools.MessageDecodingTools
import java.math.BigInteger
import java.util.concurrent.CompletableFuture

public class NetworkService<R, T : Js5GroupType>(
    private val bootstrapFactory: BootstrapFactory,
    private val ports: List<Int>,
    exp: BigInteger,
    mod: BigInteger,
    huffmanCodec: HuffmanCodec,
    js5GroupProvider: Js5GroupProvider<T>,
    public val gameMessageConsumerRepository: GameMessageConsumerRepository<R>,
    public val gameLoginHandler: GameLoginHandler,
    public val streamCipherProvider: StreamCipherProvider = DefaultStreamCipherProvider(),
    public val inetAddressValidator: InetAddressValidator = DefaultInetAddressValidator(),
    public val loginDecoderService: LoginDecoderService = DefaultLoginDecoderService(),
    public val proofOfWorkProvider: ProofOfWorkProvider<*, *> = DefaultSha256ProofOfWorkProvider(1),
    public val proofOfWorkChallengeWorker: ChallengeWorker = DefaultChallengeWorker,
    public val incomingGameMessageQueueProvider: MessageQueueProvider<IncomingGameMessage> =
        DefaultMessageQueueProvider(),
    public val outgoingGameMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage> =
        DefaultMessageQueueProvider(),
    public val gameMessageCounterProvider: GameMessageCounterProvider = DefaultGameMessageCounterProvider(),
) {
    public val decoderRepositories: MessageDecoderRepositories = MessageDecoderRepositories(exp, mod)
    public val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories()
    public val messageDecodingTools: MessageDecodingTools = MessageDecodingTools(huffmanCodec)
    public val js5Service: Js5Service<T> = Js5Service(js5GroupProvider)
    private val js5ServiceExecutor = Thread(js5Service)

    public fun start() {
        val bossGroup = bootstrapFactory.createParentLoopGroup()
        val childGroup = bootstrapFactory.createChildLoopGroup()
        val initializer =
            bootstrapFactory
                .createServerBootstrap(bossGroup, childGroup)
                .childHandler(
                    LoginChannelInitializer(this),
                )
        val futures =
            ports
                .map(initializer::bind)
                .map<ChannelFuture, CompletableFuture<Void>>(ChannelFuture::asCompletableFuture)
        CompletableFuture.allOf(*futures.toTypedArray())
            .handle { _, exception ->
                if (exception != null) {
                    bossGroup.shutdownGracefully()
                    childGroup.shutdownGracefully()
                    throw exception
                }
            }
        js5ServiceExecutor.start()
    }

    public companion object {
        // TODO: Update revision
        public const val REVISION: Int = 215
        public const val LOGIN_TIMEOUT_SECONDS: Long = 60
        public const val JS5_TIMEOUT_SECONDS: Long = 30
    }
}
