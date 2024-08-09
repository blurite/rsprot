package net.rsprot.protocol.api

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.bootstrap.BootstrapFactory
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.GameMessageHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.handlers.LoginHandlers
import net.rsprot.protocol.api.js5.Js5Configuration
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.suppliers.NpcInfoSupplier
import net.rsprot.protocol.api.suppliers.PlayerInfoSupplier
import net.rsprot.protocol.api.suppliers.WorldEntityInfoSupplier
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider

/**
 * The abstract network service factory is used to build the network service that is used
 * as the entry point to this library, allowing one to bind the network and supply everything
 * necessary network-wise from one spot.
 * @param R the receiver type that will be consuming game messages, typically a Player
 */
public abstract class AbstractNetworkServiceFactory<R> {
    /**
     * The allocator that will be used for everything in this networking library.
     * This will primarily be passed onto NPC and Player info objects, which will utilize
     * this to precompute extended info blocks and the overall main buffer.
     * It is HIGHLY recommended to use a pooled direct byte buffer if possible.
     * Pooling in particular allows us to avoid allocating new expensive 40kb buffers
     * per each player, for both player and npc infos, and direct buffers allow
     * the Netty layer to skip one copy operation to move the data off of the heap.
     */
    public open val allocator: ByteBufAllocator
        get() = PooledByteBufAllocator.DEFAULT

    /**
     * The list of ports to listen to. Typically, the server should listen to ports
     * 43594 and 443 in this section, with the 43594 port being the primary one,
     * and 443 being the fallback. 443 is additionally used for HTTP requests, should
     * those be supported.
     */
    public abstract val ports: List<Int>

    /**
     * The list of client types to register within the network.
     * If there are multiple client types implemented, one can supply multiple
     * client types in this section. It is highly likely that only one will be
     * offered though, as C++ clients are much harder to figure out.
     * Furthermore, if multiple client types are offered, it is highly recommended
     * to only register the ones you intend on supporting and using. This is because
     * each client type that is registered here additionally means we have to precompute
     * Player and NPC info extended info blocks for each of those client types,
     * meaning all that work would go to a waste if no one is there to use the types.
     */
    public abstract val supportedClientTypes: List<OldSchoolClientType>

    /**
     * Whether the client is connecting to this world under the beta world flag of
     * 65536, or 0x10000. If this is the case, the login block that the client transmits
     * differs from the usual one, as it ends up splitting CRCs up into two incomplete
     * sections. This was intended to prevent people from downloading most of the
     * beta cache without the access to the beta server, however the implementation
     * is done incorrectly and all it ends up preventing is people who use the client
     * to download the cache. The JS5 server is not notified of these constraints and
     * will server every cache group as requested regardless of the status.
     * Because of this, the implementation for the beta worlds is simplified to just
     * support logging in via a beta world, which is accomplished by immediately
     * requesting the remaining beta CRCs before passing the login request on to
     * the server. By the time the server receives information about the request,
     * all the CRCs have been obtained.
     * It is worth noting that if the client flag status differs from the server,
     * one of two possible scenarios will occur - either you will get an exception
     * as the server tries to read more bytes than what the client wrote via the CRC
     * block, or the server never receives enough bytes to consider the login block
     * complete, which means the login request will hang and eventually time out.
     */
    public open val betaWorld: Boolean
        get() = false

    /**
     * Gets the bootstrap factory used to register the network service.
     * The bootstrap factory offers the initial socket and Netty configurations
     * to be used within this library. These configurations are by default
     * made to mirror the client as much as possible.
     */
    public abstract fun getBootstrapFactory(): BootstrapFactory

    /**
     * Gets the RSA key pair that will be used to decipher the login blocks
     * sent by the client. If the keys aren't correct, the login block
     * will fail to decode, and exceptions will be thrown.
     */
    public abstract fun getRsaKeyPair(): RsaKeyPair

    /**
     * Gets the Huffman codec provider.
     * This is implemented in a provider format to allow the server to use
     * blocking implementations, which allow lazy-loading Huffman.
     * This is useful to allow binding the network early on in the boot
     * cycle without having to wait behind Huffman.
     * If a blocking implementation is used, and Huffman isn't ready when
     * it is needed, the underlying Netty thread will be blocked until it
     * is supplied. Because of this, it is recommended to use the
     * non-blocking variant in production.
     */
    public abstract fun getHuffmanCodecProvider(): HuffmanCodecProvider

    /**
     * Gets the JS5 configuration settings to be used within the JS5 service.
     * These settings allow a server to modify the frequency at which
     * groups are served to the client, as well as the ratio between
     * high priority logged in players and the low priority logged out ones,
     * allowing the JS5 protocol to send more data to those logged in
     * Furthermore, this allows defining the block size that is written
     * per client per iteration.
     * The default configuration is set to be fast enough to not show any
     * client speed reduction via localhost.
     */
    public open fun getJs5Configuration(): Js5Configuration = Js5Configuration()

    /**
     * Gets the JS5 group provider, used to return the respective byte buffers
     * or file regions from the server based on the incoming request.
     * It is fine to use lazy-loading for development, but it is highly
     * recommended to pre-compute the JS5 groups in the final form when
     * used in development, to avoid instant no-delay responses.
     */
    public abstract fun getJs5GroupProvider(): Js5GroupProvider

    /**
     * Gets the consumer repository for incoming client game prots.
     * This repository will be used to determine whether an incoming game packet
     * needs decoding in the first place - if there is no consumer for the packet
     * registered, we can simply skip the number of bytes that came in with that
     * packet, avoiding any generation of garbage. Furthermore, the consumers
     * will be automatically triggered for each incoming message when the server
     * requests it via the [Session] object that is provided during login.
     */
    public abstract fun getGameMessageConsumerRepositoryProvider(): GameMessageConsumerRepositoryProvider<R>

    /**
     * Gets the game connection handlers that are invoked whenever the client
     * logs in or reconnects. These functions are only triggered after
     * features such as Proof of Work and remaining beta archive CRCs
     * have been obtained, furthermore additional library-sided checks, such as
     * not too many connections from the same INetAddress must be met.
     * Session id is also verified by the library before passing the request
     * on to the implementing server.
     * The server is responsible for doing all the login validation at this point,
     * as well as CRC validations.
     * It is worth noting that the connection handler will be invoked from whichever
     * thread was used to decode the login block itself.
     * By default, this will be one of the threads in the ForkJoinPool.
     */
    public abstract fun getGameConnectionHandler(): GameConnectionHandler<R>

    /**
     * Gets the supplier for all the context that the NPC info protocol requires
     * to function. The server must use the avatar factory to allocate
     * avatars for each NPC that it spawned into the game. Furthermore,
     * when the respective NPC is permanently remove from the game,
     * the avatar MUST be de-allocated, otherwise that avatar will never be usable.
     * For NPCs which are simply set to respawning mode, the avatar should be kept.
     * For each player that logs into the world, one NPC info object should be
     * allocated. Similarly to the avatar, this object MUST be deallocated
     * when the player is removed from the game, be that normally or abnormally.
     */
    public abstract fun getNpcInfoSupplier(): NpcInfoSupplier

    /**
     * Gets the supplier for all the context that the Player info protocol requires
     * to function. The server must allocate one Player info object per each
     * player that logs in. Just like with NPC info, the object must be deallocated
     * when the player is removed.
     */
    public open fun getPlayerInfoSupplier(): PlayerInfoSupplier = PlayerInfoSupplier()

    public abstract fun getWorldEntityInfoSupplier(): WorldEntityInfoSupplier

    /**
     * Gets the exception handlers for channel exceptions as well as any incoming
     * game message consumers that get processed in the library.
     * Further implementations may be introduced in the future if the need arises.
     */
    public abstract fun getExceptionHandlers(): ExceptionHandlers<R>

    /**
     * Gets the handlers for anything related to INetAddresses.
     * The default implementation will keep track of the number of concurrent
     * game connections and JS5 connections separately.
     * There is furthermore a validation implementation that will by default
     * reject any connection to either service if there are 10 concurrent connections
     * to that service from the same address already.
     * The initial check is performed after the login connection, when either
     * the game login or the JS5 connection is established. For game logins
     * a secondary validation is performed right before the login block
     * is passed onto the server to handle.
     * It should be noted that the tracking mechanism is fairly straightforward
     * and doesn't cost much in performance.
     */
    public open fun getINetAddressHandlers(): INetAddressHandlers = INetAddressHandlers()

    /**
     * Gets the handlers for game messages, which includes providing
     * queue implementations for the incoming and outgoing messages,
     * as well as a counter for incoming game messages, to avoid processing
     * too many packets per cycle.
     * The default implementation offers a ConcurrentLinkedQueue for both of
     * the queues, and has a limit of 10 user packets, and 50 client packets.
     * Only packets for which the server has registered a listener will be counted
     * towards these limitations. Most packets will count towards the 10 user packets,
     * as the client limitation is strictly for packets which cannot directly
     * be influenced by the user.
     * If either of the limitations is hit during message decoding, the decoding
     * is halted immediately until after the game has polled the incoming messages.
     * This means the TCP protocol is responsible for ensuring too much data cannot
     * be passed onto us.
     */
    public open fun getGameMessageHandlers(): GameMessageHandlers = GameMessageHandlers()

    /**
     * Gets the handlers for the login related things.
     * This includes an implementation for generating the initial session ids,
     * which by default uses a secure random implementation.
     * Additional configurations support modifying the stream cipher, proof of work
     * logic, and a service for decoding logins, which is invoked using ForkJoinPool
     * by default.
     * The Proof of Work implementation uses the same inputs as used in the OldSchool
     * as of writing this. It is possible to disable Proof of Work entirely, which
     * can be useful for development environments.
     */
    public open fun getLoginHandlers(): LoginHandlers = LoginHandlers()

    /**
     * Builds a network service through this factoring, using all
     * the information provided in here.
     */
    public fun build(): NetworkService<R> {
        val allocator = this.allocator
        val ports = this.ports
        val supportedClientTypes = this.supportedClientTypes
        val huffman = getHuffmanCodecProvider()
        val entityInfoProtocols =
            EntityInfoProtocols.initialize(
                allocator,
                supportedClientTypes,
                huffman,
                getPlayerInfoSupplier(),
                getNpcInfoSupplier(),
                getWorldEntityInfoSupplier(),
            )
        return NetworkService(
            allocator,
            ports,
            betaWorld,
            getBootstrapFactory(),
            entityInfoProtocols,
            supportedClientTypes,
            getGameConnectionHandler(),
            getExceptionHandlers(),
            getINetAddressHandlers(),
            getGameMessageHandlers(),
            getLoginHandlers(),
            huffman,
            getGameMessageConsumerRepositoryProvider(),
            getRsaKeyPair(),
            getJs5Configuration(),
            getJs5GroupProvider(),
        )
    }
}
