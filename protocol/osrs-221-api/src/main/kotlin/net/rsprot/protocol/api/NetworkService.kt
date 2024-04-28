package net.rsprot.protocol.api

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
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
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcIndexSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
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

@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("MemberVisibilityCanBePrivate")
public class NetworkService<R, T : Js5GroupType>(
    private val bootstrapFactory: BootstrapFactory,
    private val ports: List<Int>,
    private val clientTypes: List<OldSchoolClientType>,
    private val exp: BigInteger,
    private val mod: BigInteger,
    private val huffmanCodec: HuffmanCodec,
    private val js5GroupProvider: Js5GroupProvider<T>,
    public val gameMessageConsumerRepository: GameMessageConsumerRepository<R>,
    public val gameLoginHandler: GameLoginHandler,
    private val npcIndexSupplier: NpcIndexSupplier,
    private val allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT,
    private val playerExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
    private val npcExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
    private val playerInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    private val npcInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
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

    public lateinit var playerAvatarFactory: PlayerAvatarFactory
    public lateinit var playerInfoProtocol: PlayerInfoProtocol
    public lateinit var npcAvatarFactory: NpcAvatarFactory
    public lateinit var npcInfoProtocol: NpcInfoProtocol

    @ExperimentalUnsignedTypes
    public fun start() {
        verifyClientTypesAreImplemented()
        initializeInfos()
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

    private fun initializeInfos() {
        val playerWriters = mutableListOf<PlayerAvatarExtendedInfoWriter>()
        val npcWriters = mutableListOf<NpcAvatarExtendedInfoWriter>()
        val npcResolutionChangeEncoders = mutableListOf<NpcResolutionChangeEncoder>()
        if (OldSchoolClientType.DESKTOP in clientTypes) {
            playerWriters += PlayerAvatarExtendedInfoDesktopWriter()
            npcWriters += NpcAvatarExtendedInfoDesktopWriter()
            npcResolutionChangeEncoders += DesktopLowResolutionChangeEncoder()
        }
        this.playerAvatarFactory =
            PlayerAvatarFactory(
                allocator,
                playerExtendedInfoFilter,
                playerWriters,
                huffmanCodec,
            )
        this.playerInfoProtocol =
            PlayerInfoProtocol(
                allocator,
                playerInfoProtocolWorker,
                this.playerAvatarFactory,
            )
        this.npcAvatarFactory =
            NpcAvatarFactory(
                allocator,
                npcExtendedInfoFilter,
                npcWriters,
                huffmanCodec,
            )
        this.npcInfoProtocol =
            NpcInfoProtocol(
                allocator,
                npcIndexSupplier,
                ClientTypeMap.of(
                    npcResolutionChangeEncoders,
                    OldSchoolClientType.COUNT,
                ) {
                    it.clientType
                },
                npcAvatarFactory,
                npcInfoProtocolWorker,
            )
    }

    private fun verifyClientTypesAreImplemented() {
        check(OldSchoolClientType.IOS !in clientTypes) {
            "iOS is not currently supported."
        }
        check(OldSchoolClientType.ANDROID !in clientTypes) {
            "Android is not currently supported."
        }
    }

    public fun isSupported(clientType: OldSchoolClientType): Boolean {
        return clientType in clientTypes
    }

    override fun toString(): String {
        return "NetworkService(" +
            "bootstrapFactory=$bootstrapFactory, " +
            "ports=$ports, " +
            "clientTypes=$clientTypes, " +
            "exp=$exp, " +
            "mod=$mod, " +
            "huffmanCodec=$huffmanCodec, " +
            "js5GroupProvider=$js5GroupProvider, " +
            "gameMessageConsumerRepository=$gameMessageConsumerRepository, " +
            "gameLoginHandler=$gameLoginHandler, " +
            "npcIndexSupplier=$npcIndexSupplier, " +
            "allocator=$allocator, " +
            "playerExtendedInfoFilter=$playerExtendedInfoFilter, " +
            "npcExtendedInfoFilter=$npcExtendedInfoFilter, " +
            "playerInfoProtocolWorker=$playerInfoProtocolWorker, " +
            "npcInfoProtocolWorker=$npcInfoProtocolWorker, " +
            "streamCipherProvider=$streamCipherProvider, " +
            "inetAddressValidator=$inetAddressValidator, " +
            "loginDecoderService=$loginDecoderService, " +
            "proofOfWorkProvider=$proofOfWorkProvider, " +
            "proofOfWorkChallengeWorker=$proofOfWorkChallengeWorker, " +
            "incomingGameMessageQueueProvider=$incomingGameMessageQueueProvider, " +
            "outgoingGameMessageQueueProvider=$outgoingGameMessageQueueProvider, " +
            "gameMessageCounterProvider=$gameMessageCounterProvider, " +
            "decoderRepositories=$decoderRepositories, " +
            "encoderRepositories=$encoderRepositories, " +
            "messageDecodingTools=$messageDecodingTools, " +
            "js5Service=$js5Service, " +
            "js5ServiceExecutor=$js5ServiceExecutor, " +
            "playerAvatarFactory=$playerAvatarFactory, " +
            "playerInfoProtocol=$playerInfoProtocol, " +
            "npcAvatarFactory=$npcAvatarFactory, " +
            "npcInfoProtocol=$npcInfoProtocol" +
            ")"
    }

    public companion object {
        public const val REVISION: Int = 221
        public const val LOGIN_TIMEOUT_SECONDS: Long = 60
        public const val JS5_TIMEOUT_SECONDS: Long = 30
    }
}
