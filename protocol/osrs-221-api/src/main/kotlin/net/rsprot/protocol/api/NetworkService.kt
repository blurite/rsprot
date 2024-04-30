package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelFuture
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.api.bootstrap.BootstrapFactory
import net.rsprot.protocol.api.implementation.DefaultGameMessageCounterProvider
import net.rsprot.protocol.api.implementation.DefaultInetAddressTracker
import net.rsprot.protocol.api.implementation.DefaultInetAddressValidator
import net.rsprot.protocol.api.implementation.DefaultLoginDecoderService
import net.rsprot.protocol.api.implementation.DefaultMessageQueueProvider
import net.rsprot.protocol.api.implementation.DefaultSessionIdGenerator
import net.rsprot.protocol.api.implementation.DefaultStreamCipherProvider
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.api.js5.Js5Service
import net.rsprot.protocol.api.repositories.MessageDecoderRepositories
import net.rsprot.protocol.api.repositories.MessageEncoderRepositories
import net.rsprot.protocol.api.util.asCompletableFuture
import net.rsprot.protocol.client.ClientType
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.game.incoming.prot.DesktopGameMessageDecoderRepository
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder
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
import net.rsprot.protocol.message.ZoneProt
import net.rsprot.protocol.message.codec.UpdateZonePartialEnclosedCache
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider
import net.rsprot.protocol.tools.MessageDecodingTools
import java.math.BigInteger
import java.util.EnumMap
import java.util.concurrent.CompletableFuture
import kotlin.time.measureTime

@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("MemberVisibilityCanBePrivate")
public class NetworkService<R, T : Js5GroupType>
    @JvmOverloads
    public constructor(
        private val bootstrapFactory: BootstrapFactory,
        private val ports: List<Int>,
        public val clientTypes: List<OldSchoolClientType>,
        private val exp: BigInteger,
        private val mod: BigInteger,
        public val huffmanCodecProvider: HuffmanCodecProvider,
        private val js5GroupProvider: Js5GroupProvider<T>,
        public val gameMessageConsumerRepositoryProvider: GameMessageConsumerRepositoryProvider<R>,
        public val gameConnectionHandler: GameConnectionHandler<R>,
        private val npcIndexSupplier: NpcIndexSupplier,
        private val allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT,
        private val playerExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
        private val npcExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
        private val playerInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
        private val npcInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
        public val streamCipherProvider: StreamCipherProvider = DefaultStreamCipherProvider(),
        public val sessionIdGenerator: SessionIdGenerator = DefaultSessionIdGenerator(),
        public val inetAddressValidator: InetAddressValidator = DefaultInetAddressValidator(),
        public val js5InetAddressTracker: InetAddressTracker = DefaultInetAddressTracker(),
        public val gameInetAddressTracker: InetAddressTracker = DefaultInetAddressTracker(),
        public val loginDecoderService: LoginDecoderService = DefaultLoginDecoderService(),
        public val proofOfWorkProvider: ProofOfWorkProvider<*, *> = DefaultSha256ProofOfWorkProvider(1),
        public val proofOfWorkChallengeWorker: ChallengeWorker = DefaultChallengeWorker,
        public val incomingGameMessageQueueProvider: MessageQueueProvider<IncomingGameMessage> =
            DefaultMessageQueueProvider(),
        public val outgoingGameMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage> =
            DefaultMessageQueueProvider(),
        public val gameMessageCounterProvider: GameMessageCounterProvider = DefaultGameMessageCounterProvider(),
    ) {
        public lateinit var decoderRepositories: MessageDecoderRepositories
        public val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories()
        public val messageDecodingTools: MessageDecodingTools = MessageDecodingTools(huffmanCodecProvider)
        public val js5Service: Js5Service<T> = Js5Service(js5GroupProvider)
        private val js5ServiceExecutor = Thread(js5Service)
        private lateinit var updateZonePartialEnclosedCacheClientTypeMap: ClientTypeMap<UpdateZonePartialEnclosedCache>

        public lateinit var playerAvatarFactory: PlayerAvatarFactory
        public lateinit var playerInfoProtocol: PlayerInfoProtocol
        public lateinit var npcAvatarFactory: NpcAvatarFactory
        public lateinit var npcInfoProtocol: NpcInfoProtocol

        @ExperimentalUnsignedTypes
        @ExperimentalStdlibApi
        public fun start() {
            verifyClientTypesAreImplemented()
            val time =
                measureTime {
                    initializeDecoderRepositories()
                    initializeInfos()
                    initializeUpdateZonePartialEnclosedCacheClientMap()
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
            logger.info { "Started in: $time" }
            logger.info { "Bound to ports: ${ports.joinToString(", ")}" }
            logger.info { "Revision: $REVISION" }
            val clientTypeNames =
                clientTypes.joinToString(", ") {
                    it.name.lowercase().replaceFirstChar(Char::uppercase)
                }
            logger.info { "Supported client types: $clientTypeNames" }
        }

        private fun initializeUpdateZonePartialEnclosedCacheClientMap() {
            val list = mutableListOf<Pair<ClientType, UpdateZonePartialEnclosedCache>>()
            if (OldSchoolClientType.DESKTOP in clientTypes) {
                list += OldSchoolClientType.DESKTOP to DesktopUpdateZonePartialEnclosedEncoder
            }
            this.updateZonePartialEnclosedCacheClientTypeMap = ClientTypeMap.of(OldSchoolClientType.COUNT, list)
        }

        public fun <T : ZoneProt> computeUpdateZonePartialEnclosedCache(
            events: List<T>,
        ): EnumMap<OldSchoolClientType, ByteBuf> {
            val map = EnumMap<OldSchoolClientType, ByteBuf>(OldSchoolClientType::class.java)
            for (type in clientTypes) {
                map[type] = this.updateZonePartialEnclosedCacheClientTypeMap[type].buildCache(allocator, events)
            }
            return map
        }

        @ExperimentalStdlibApi
        private fun initializeDecoderRepositories() {
            val list = mutableListOf<Pair<OldSchoolClientType, MessageDecoderRepository<GameClientProt>>>()
            if (OldSchoolClientType.DESKTOP in clientTypes) {
                list += OldSchoolClientType.DESKTOP to DesktopGameMessageDecoderRepository.build()
            }
            val map =
                ClientTypeMap.of(
                    OldSchoolClientType.COUNT,
                    list,
                )
            this.decoderRepositories = MessageDecoderRepositories(exp, mod, map)
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
                    huffmanCodecProvider,
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
                    huffmanCodecProvider,
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
                "huffmanCodec=$huffmanCodecProvider, " +
                "js5GroupProvider=$js5GroupProvider, " +
                "gameMessageConsumerRepositoryProvider=$gameMessageConsumerRepositoryProvider, " +
                "gameLoginHandler=$gameConnectionHandler, " +
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
            private val logger = InlineLogger()
        }
    }
