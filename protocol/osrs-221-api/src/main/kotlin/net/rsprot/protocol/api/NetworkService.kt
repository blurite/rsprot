package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelFuture
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.bootstrap.BootstrapFactory
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.GameMessageHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.handlers.LoginHandlers
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.api.js5.Js5Service
import net.rsprot.protocol.api.repositories.MessageDecoderRepositories
import net.rsprot.protocol.api.repositories.MessageEncoderRepositories
import net.rsprot.protocol.api.util.asCompletableFuture
import net.rsprot.protocol.client.ClientType
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.message.ZoneProt
import net.rsprot.protocol.message.codec.UpdateZonePartialEnclosedCache
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider
import net.rsprot.protocol.tools.MessageDecodingTools
import java.util.EnumMap
import java.util.concurrent.CompletableFuture
import kotlin.time.measureTime

@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("MemberVisibilityCanBePrivate")
public class NetworkService<R, T : Js5GroupType>
    internal constructor(
        internal val allocator: ByteBufAllocator,
        internal val ports: List<Int>,
        internal val bootstrapFactory: BootstrapFactory,
        internal val entityInfoProtocols: EntityInfoProtocols,
        internal val clientTypes: List<OldSchoolClientType>,
        internal val gameConnectionHandler: GameConnectionHandler<R>,
        internal val exceptionHandlers: ExceptionHandlers<R>,
        internal val iNetAddressHandlers: INetAddressHandlers,
        internal val gameMessageHandlers: GameMessageHandlers,
        internal val loginHandlers: LoginHandlers,
        public val huffmanCodecProvider: HuffmanCodecProvider,
        public val gameMessageConsumerRepositoryProvider: GameMessageConsumerRepositoryProvider<R>,
        rsaKeyPair: RsaKeyPair,
        js5GroupProvider: Js5GroupProvider<T>,
    ) {
        internal val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories()
        internal val messageDecodingTools: MessageDecodingTools = MessageDecodingTools(huffmanCodecProvider)
        internal val js5Service: Js5Service<T> = Js5Service(js5GroupProvider)
        private val js5ServiceExecutor = Thread(js5Service)
        private val updateZonePartialEnclosedCacheClientTypeMap:
            ClientTypeMap<UpdateZonePartialEnclosedCache> = initializeUpdateZonePartialEnclosedCacheClientMap()
        internal val decoderRepositories: MessageDecoderRepositories =
            MessageDecoderRepositories.initialize(
                clientTypes,
                rsaKeyPair,
            )
        public val playerAvatarFactory: PlayerAvatarFactory get() = entityInfoProtocols.playerAvatarFactory
        public val playerInfoProtocol: PlayerInfoProtocol get() = entityInfoProtocols.playerInfoProtocol
        public val npcAvatarFactory: NpcAvatarFactory get() = entityInfoProtocols.npcAvatarFactory
        public val npcInfoProtocol: NpcInfoProtocol get() = entityInfoProtocols.npcInfoProtocol

        @ExperimentalUnsignedTypes
        @ExperimentalStdlibApi
        public fun start() {
            val time =
                measureTime {
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

        private fun initializeUpdateZonePartialEnclosedCacheClientMap(): ClientTypeMap<UpdateZonePartialEnclosedCache> {
            val list = mutableListOf<Pair<ClientType, UpdateZonePartialEnclosedCache>>()
            if (OldSchoolClientType.DESKTOP in clientTypes) {
                list += OldSchoolClientType.DESKTOP to DesktopUpdateZonePartialEnclosedEncoder
            }
            return ClientTypeMap.of(OldSchoolClientType.COUNT, list)
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

        public fun isSupported(clientType: OldSchoolClientType): Boolean {
            return clientType in clientTypes
        }

        public companion object {
            public const val REVISION: Int = 221
            public const val LOGIN_TIMEOUT_SECONDS: Long = 60
            public const val JS5_TIMEOUT_SECONDS: Long = 30
            private val logger = InlineLogger()
        }
    }
