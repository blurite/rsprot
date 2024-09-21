package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.GameMessageHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.handlers.LoginHandlers
import net.rsprot.protocol.api.js5.Js5Configuration
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.js5.Js5Service
import net.rsprot.protocol.api.repositories.MessageDecoderRepositories
import net.rsprot.protocol.api.repositories.MessageEncoderRepositories
import net.rsprot.protocol.api.util.asCompletableFuture
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarFactory
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import kotlin.time.measureTime

/**
 * The primary network service implementation that brings all the necessary components together
 * in a single "god" object.
 * @param R the receiver type for the incoming game message consumers, typically a player
 * @property allocator the byte buffer allocator used throughout the library
 * @property ports the list of ports that the service will connect to
 * @property betaWorld whether this world is a beta world
 * @property bootstrapBuilder the bootstrap builder used to configure the socket and Netty
 * @property entityInfoProtocols a wrapper object to bring together player and NPC info protocols
 * @property clientTypes the list of client types that were registered
 * @property gameConnectionHandler the handler for game logins and reconnections
 * @property exceptionHandlers the wrapper object for any exception handlers that the server must provide
 * @property iNetAddressHandlers the wrapper object to handle anything to do with tracking and rejecting
 * network addresses trying to establish connections
 * @property gameMessageHandlers the wrapper object for anything to do with game packets post-login
 * @property huffmanCodecProvider the provider for Huffman codecs, used to compress the text
 * in some packets
 * @property gameMessageConsumerRepositoryProvider the consumer repository for game messages.
 * This is made public in case a blocking implementation is used, in which case the
 * repository may be lazy-initialized into this library.
 * @param rsaKeyPair the key pair for RSA to decode login blocks
 * @param js5Configuration the configuration used by the JS5 service to determine the exact conditions
 * for serving any connected clients
 * @param js5GroupProvider the provider for any JS5 requests that the client makes
 * @property encoderRepositories the encoder repositories for all the connection types
 * @property js5Service the service behind the JS5, serving all connected clients fairly
 * @property js5ServiceExecutor the thread executing the JS5 service. Since the main JS5
 * service is fairly lightweight and doesn't actually process much, a single thread
 * is more than sufficient here. Utilizing more threads makes implementing a fair JS5
 * service significantly more difficult.
 * @property decoderRepositories the repositories for decoding all the incoming client packets
 * @property playerInfoProtocol the protocol responsible for tracking and computing player info
 * for all the players in the game
 * @property npcAvatarFactory the avatar factory for NPCs responsible for tracking anything
 * necessary to represent a NPC to the client
 * @property npcInfoProtocol the protocol responsible for tracking and computing everything related
 * to the NPC info packet for every player
 */
@Suppress("MemberVisibilityCanBePrivate")
public class NetworkService<R>
    internal constructor(
        internal val allocator: ByteBufAllocator,
        internal val ports: List<Int>,
        internal val betaWorld: Boolean,
        internal val bootstrapBuilder: BootstrapBuilder,
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
        js5Configuration: Js5Configuration,
        js5GroupProvider: Js5GroupProvider,
    ) {
        internal val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories(huffmanCodecProvider)
        internal val js5Service: Js5Service = Js5Service(js5Configuration, js5GroupProvider)
        private val js5ServiceExecutor = Thread(js5Service)
        internal val decoderRepositories: MessageDecoderRepositories =
            MessageDecoderRepositories.initialize(
                clientTypes,
                rsaKeyPair,
                huffmanCodecProvider,
            )
        public val playerInfoProtocol: PlayerInfoProtocol
            get() = entityInfoProtocols.playerInfoProtocol
        public val npcAvatarFactory: NpcAvatarFactory
            get() = entityInfoProtocols.npcAvatarFactory
        public val npcInfoProtocol: NpcInfoProtocol
            get() = entityInfoProtocols.npcInfoProtocol
        public val worldEntityAvatarFactory: WorldEntityAvatarFactory
            get() = entityInfoProtocols.worldEntityAvatarFactory
        public val worldEntityInfoProtocol: WorldEntityProtocol
            get() = entityInfoProtocols.worldEntityInfoProtocol

        private lateinit var bossGroup: EventLoopGroup
        private lateinit var childGroup: EventLoopGroup
        private lateinit var js5PrefetchFuture: ScheduledFuture<*>

        /**
         * Starts the network service by binding the provided ports.
         * If any of them fail, the service is shut down and the exception is propagated forward.
         */
        @ExperimentalUnsignedTypes
        @ExperimentalStdlibApi
        public fun start() {
            val time =
                measureTime {
                    val bootstrap = bootstrapBuilder.build()
                    val initializer =
                        bootstrap.childHandler(
                            LoginChannelInitializer(this),
                        )
                    this.bossGroup = initializer.config().group()
                    this.childGroup = initializer.config().childGroup()
                    val futures =
                        ports
                            .map(initializer::bind)
                            .map<ChannelFuture, CompletableFuture<Void>>(ChannelFuture::asCompletableFuture)
                    val future =
                        CompletableFuture
                            .allOf(*futures.toTypedArray())
                            .handle { _, exception ->
                                if (exception != null) {
                                    bossGroup.shutdownGracefully()
                                    childGroup.shutdownGracefully()
                                    throw exception
                                }
                            }
                    js5ServiceExecutor.start()
                    js5PrefetchFuture = Js5Service.startPrefetching(js5Service)
                    try {
                        // join it, which will propagate any exceptions
                        future.join()
                    } catch (t: Throwable) {
                        js5Service.triggerShutdown()
                        throw t
                    }
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

        public fun shutdown() {
            logger.info { "Attempting to shut down network service." }
            js5Service.triggerShutdown()
            js5PrefetchFuture.cancel(true)
            bossGroup.shutdownGracefully()
            childGroup.shutdownGracefully()
            logger.info { "Network service successfully shut down." }
        }

        /**
         * Checks whether the provided [clientType] is supported by the service.
         */
        public fun isSupported(clientType: OldSchoolClientType): Boolean = clientType in clientTypes

        public companion object {
            public const val REVISION: Int = 225
            public const val LOGIN_TIMEOUT_SECONDS: Long = 60
            public const val JS5_TIMEOUT_SECONDS: Long = 30
            private val logger = InlineLogger()
        }
    }
