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
import net.rsprot.protocol.api.js5.Js5Configuration
import net.rsprot.protocol.api.js5.Js5GroupProvider
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
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarFactory
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol
import net.rsprot.protocol.message.ZoneProt
import net.rsprot.protocol.message.codec.UpdateZonePartialEnclosedCache
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider
import net.rsprot.protocol.tools.MessageDecodingTools
import java.util.ArrayDeque
import java.util.EnumMap
import java.util.LinkedList
import java.util.concurrent.CompletableFuture
import kotlin.time.measureTime

/**
 * The primary network service implementation that brings all the necessary components together
 * in a single "god" object.
 * @param R the receiver type for the incoming game message consumers, typically a player
 * @property allocator the byte buffer allocator used throughout the library
 * @property ports the list of ports that the service will connect to
 * @property betaWorld whether this world is a beta world
 * @property bootstrapFactory the bootstrap factory used to configure the socket and Netty
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
 * @property messageDecodingTools the set of message decoding tools, which currently
 * only includes Huffman, but may require more instances in the future. This is passed to all
 * the message decoders.
 * @property js5Service the service behind the JS5, serving all connected clients fairly
 * @property js5ServiceExecutor the thread executing the JS5 service. Since the main JS5
 * service is fairly lightweight and doesn't actually process much, a single thread
 * is more than sufficient here. Utilizing more threads makes implementing a fair JS5
 * service significantly more difficult.
 * @property updateZonePartialEnclosedCacheClientTypeMap the map of update zone partial enclosed
 * cache builders for each client type registered. This is because the events in a zone are
 * computed once for all observers and written to all of them in one go.
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
        js5Configuration: Js5Configuration,
        js5GroupProvider: Js5GroupProvider,
    ) {
        internal val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories()
        internal val messageDecodingTools: MessageDecodingTools = MessageDecodingTools(huffmanCodecProvider)
        internal val js5Service: Js5Service = Js5Service(js5Configuration, js5GroupProvider)
        private val js5ServiceExecutor = Thread(js5Service)
        private val updateZonePartialEnclosedCacheClientTypeMap:
            ClientTypeMap<UpdateZonePartialEnclosedCache> = initializeUpdateZonePartialEnclosedCacheClientMap()
        internal val decoderRepositories: MessageDecoderRepositories =
            MessageDecoderRepositories.initialize(
                clientTypes,
                rsaKeyPair,
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

        /**
         * Starts the network service by binding the provided ports.
         * If any of them fail, the service is shut down and the exception is propagated forward.
         */
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
                    Js5Service.startPrefetching(js5Service)
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

        private fun initializeUpdateZonePartialEnclosedCacheClientMap(): ClientTypeMap<UpdateZonePartialEnclosedCache> {
            val list = mutableListOf<Pair<ClientType, UpdateZonePartialEnclosedCache>>()
            if (OldSchoolClientType.DESKTOP in clientTypes) {
                list += OldSchoolClientType.DESKTOP to DesktopUpdateZonePartialEnclosedEncoder
            }
            return ClientTypeMap.of(OldSchoolClientType.COUNT, list)
        }

        private val updateZonePartialEnclosedBufferList = ArrayDeque<LinkedList<ByteBuf>>()
        private var currentUpdateOnePartialEnclosedBuffers = LinkedList<ByteBuf>()
        private var currentZoneCallCount = 0

        /**
         * Computes the buffer for the update zone partial enclosed packet payload for
         * a given list of events, for every platform that is registered.
         * @param events the list of zone prot events in a given zone
         * @return a client type map that contains buffers for every client type that was
         * initially registered. The server should write the same buffer for every observing client.
         * For the enclosed packets, the reference count is not increased or decreased per observer,
         * nor are the writer and reader indices modified. The server must, however, release these buffers
         * at the end of the cycle, when they have been written to each client.
         */
        public fun <T : ZoneProt> computeUpdateZonePartialEnclosedCache(
            events: List<T>,
        ): EnumMap<OldSchoolClientType, ByteBuf> {
            val map = EnumMap<OldSchoolClientType, ByteBuf>(OldSchoolClientType::class.java)
            for (type in clientTypes) {
                val buffer = this.updateZonePartialEnclosedCacheClientTypeMap[type].buildCache(allocator, events)
                map[type] = buffer
                currentUpdateOnePartialEnclosedBuffers += buffer
            }
            // If there's an absurd amount of buffers in the list, it probably means the server
            // hasn't been releasing them via the postUpdate function.
            // If this is the case, it is only a matter of time before the server runs out of
            // memory for the buffers.
            // In here, the maximum is set to exactly 1 gigabyte allocated by these buffers
            // In normal circumstances, it should never hit this scenario, as
            // it requires 25,000 unique zones to have a partial enclosed buffer
            // which is basically an eight of the entire game map as a whole, excluding instances.
            if (++currentZoneCallCount >= 25_000) {
                logger.warn {
                    "Update zone partial enclosed buffers have not been correctly released!"
                }
            }
            return map
        }

        /**
         * Clear any update zone partial enclosed prebuilt buffers that no longer have
         * any references to them, meaning all the observers' netty channels have
         * had these buffers written out. If a buffer is requested to be written over, but
         * the socket closes or the player logs out before that can actually hit the
         * encoder, the ref count will never be released. In these cases, a fail-safe
         * mechanism is utilized to release the buffer after an entire minute has passed.
         * Under normal circumstances, it should never get into a scenario where this
         * is hit.
         */
        public fun postUpdate() {
            currentZoneCallCount = 0
            val bufferList = this.updateZonePartialEnclosedBufferList
            if (bufferList.size >= 100) {
                val first = bufferList.removeFirst()
                releaseBuffers(first, true)
            }
            val curBuf = this.currentUpdateOnePartialEnclosedBuffers
            if (curBuf.size > 0) {
                bufferList.addLast(curBuf)
                this.currentUpdateOnePartialEnclosedBuffers = LinkedList()
            }
            for (buffers in bufferList) {
                // Just release the buffers, the linked list may become empty as a result
                // But this is fine, we have a rotating fixed-size 100 list anyhow.
                releaseBuffers(buffers, false)
            }
        }

        /**
         * Releases the buffers in the linked list if they no longer have any references,
         * meaning all the Netty encoders have transferred the data over to the respective
         * channels. This state is reached whenever the ref count reaches 1.
         * If a buffer is allocated for a player, but the session closes before it can
         * be transferred over, the reference count will never be decreases.
         * In such scenarios, a safety mechanism will be used to forcibly release the buffer
         * after one minute has passed (based on the number of postUpdate() calls).
         * In normal circumstances, the encoder should be triggered in under one cycle,
         * so this should absolutely never be hit in normal circumstances, and if it is,
         * it implies there's a bigger underlying problem and that connection is doomed
         * anyhow.
         * @param buffers the linked list of buffers that were computed in the past
         * @param forceRelease whether to force release all the buffers, this is only done
         * after one minute has passed and the buffer has not been released still.
         */
        private fun releaseBuffers(
            buffers: LinkedList<ByteBuf>,
            forceRelease: Boolean,
        ) {
            if (buffers.isEmpty()) {
                return
            }
            val iterator = buffers.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                val refCount = next.refCnt()
                if (forceRelease) {
                    // Don't bother removing from the list if force removing,
                    // let the garbage collector deal with it
                    if (refCount > 0) {
                        next.release(refCount)
                    }
                    continue
                }
                if (refCount > 1) {
                    continue
                }
                if (refCount == 1) {
                    next.release()
                }
                iterator.remove()
            }
        }

        /**
         * Checks whether the provided [clientType] is supported by the service.
         */
        public fun isSupported(clientType: OldSchoolClientType): Boolean = clientType in clientTypes

        public companion object {
            public const val REVISION: Int = 223
            public const val LOGIN_TIMEOUT_SECONDS: Long = 60
            public const val JS5_TIMEOUT_SECONDS: Long = 30
            private val logger = InlineLogger()
        }
    }
