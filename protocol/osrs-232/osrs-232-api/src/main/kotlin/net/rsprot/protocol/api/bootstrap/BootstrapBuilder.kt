package net.rsprot.protocol.api.bootstrap

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.uring.IoUring
import io.netty.channel.uring.IoUringIoHandler
import io.netty.channel.uring.IoUringServerSocketChannel
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder.EventLoopGroupType.EPOLL
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder.EventLoopGroupType.IOURING
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder.EventLoopGroupType.KQUEUE
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder.EventLoopGroupType.NIO
import net.rsprot.protocol.api.handlers.OutgoingMessageSizeEstimator
import java.text.NumberFormat
import kotlin.math.max

/**
 * A bootstrap builder responsible for generating the Netty bootstrap
 */
public class BootstrapBuilder {
    /**
     * An enum of possible event loop group types which one could choose between.
     * @property IOURING an asynchronous event loop group for the Linux kernels.
     * @property EPOLL an event loop group for the Linux kernels.
     * @property KQUEUE an event loop group for BSD (FreeBSD / OpenBSD) and
     * Darwin (Mac OS X / iOS) kernels.
     * @property NIO an event loop group by the JVM, available on all platforms.
     */
    public enum class EventLoopGroupType {
        IOURING,
        EPOLL,
        KQUEUE,
        NIO,
    }

    private var allocator: ByteBufAllocator? = null
    private var bossThreadCount: Int? = null
    private var childThreadCount: Int? = null
    private var soRcvBufSize: Int? = null
    private var soSndBufSize: Int? = null
    private var writeBufferWatermarkLow: Int? = null
    private var writeBufferWatermarkHigh: Int? = null
    private var tcpNoDelay: Boolean? = null
    private var soBacklog: Int? = null
    private var soReuseAddress: Boolean? = null
    private var eventLoopGroupTypes: Array<out EventLoopGroupType>? = null

    /**
     * Sets the default byte buffer allocator that is used throughout RSProt for incoming
     * and outgoing messages.
     * The default value is [ByteBufAllocator.DEFAULT], which boils down to a pooled
     * direct byte buffer allocator by default, as long as it is available, otherwise
     * the pooled heap byte buffer allocator is used. It is possible to switch the underlying
     * type via system properties, so the end result may not be as described.
     * @param alloc the byte buffer allocator used for all buffers.
     */
    public fun allocator(alloc: ByteBufAllocator): BootstrapBuilder {
        this.allocator = alloc
        return this
    }

    /**
     * Sets the boss thread count to the specified [threadCount]. If the [threadCount] is 0,
     * Netty will use a number equal to the number of physical CPU threads the server has.
     *
     * The default value for boss thread count is 1. It should be noted that there isn't any
     * benefit to increasing the boss thread count, as this merely accepts new incoming connections,
     * accepting too much at once might overload the child threads and cause more harm anyway.
     *
     * @param threadCount the number of threads to use.
     */
    public fun bossThreadCount(threadCount: Int): BootstrapBuilder {
        this.bossThreadCount = threadCount
        return this
    }

    /**
     * Sets the child thread count to the specified [threadCount]. If the [threadCount] is 0,
     * Netty will use a number equal to the number of physical CPU threads the server has.
     *
     * The default value for child thread count is `physicalThreadCount - 2`, with a minimum of 1.
     * This means at least one CPU core will be free to handle the rest of the processes,
     * which should keep the application usable even if it is getting attacked by denial of service
     * type attacks. Utilizing the entire CPU could essentially turn the application unusable and
     * cause more harm than good. The general logic here is that if Netty requires every single
     * thread your server has to offer, your server is going to die anyhow, so it's best to try
     * and keep it at least usable.
     *
     * @param threadCount the number of threads to use.
     */
    public fun childThreadCount(threadCount: Int): BootstrapBuilder {
        this.childThreadCount = threadCount
        return this
    }

    /**
     * Sets the socket receive buffer size at kernel level, telling the system what kind of
     * buffers to use for incoming traffic.
     * The default value is 65536 bytes.
     *
     * @param numBytes the number of bytes used for the buffer at kernel level.
     */
    public fun socketReceiveBufferSize(numBytes: Int): BootstrapBuilder {
        this.soRcvBufSize = numBytes
        return this
    }

    /**
     * Sets the socket send buffer size at kernel level, telling the system what kind of
     * buffers to use for outgoing traffic.
     * The default value is 65536 bytes.
     *
     * @param numBytes the number of bytes used for the buffer at kernel level.
     */
    public fun socketSendBufferSize(numBytes: Int): BootstrapBuilder {
        this.soSndBufSize = numBytes
        return this
    }

    /**
     * Sets the buffer watermarks for a given channel, indicating when the server should stop
     * trying to write more bytes into the channel and when to continue.
     *
     * An important note is that watermarks are based off of [io.netty.channel.MessageSizeEstimator]
     * implementations, not the underlying packet itself. For any unknown messages (anything that
     * isn't a [io.netty.buffer.ByteBuf] or [io.netty.buffer.ByteBufHolder]), the size is estimated
     * to be 8 bytes, this can easily become problematic if the actual message holds onto hundreds
     * of kilobytes, for example. Starting in revision 225, RSProt will accurately estimate the size
     * of messages. For any older revisions, anything that holds a byte buffer, which is most of the
     * larger messages, will be implemented via a [io.netty.buffer.DefaultByteBufHolder], ensuring
     * that the big problem cases will not cause major miscalculations. Anything else, however,
     * will just be assumed to be 8 bytes, which likely is a good estimate anyhow.
     *
     * The largest possible outgoing message, as of revision 225, is roughly 630kb, via a JS5 group
     * for the client background.
     *
     * @param numLowBytes the number of bytes waiting to be flushed at which the channel
     * becomes writable again as dictated by [io.netty.channel.Channel.isWritable].
     * The default value for the low watermark is 524,288 bytes.
     * @param numHighBytes the number of bytes waiting to be flushed at which the channel
     * becomes **un-writable**. It should be noted that this is merely a suggestion, not enforced
     * by Netty. The implementation (in this case, RSProt) must be responsible for not trying
     * to write any more bytes into the channel after that point. Netty will accept any number
     * of bytes forced into it, even if [io.netty.channel.Channel.isWritable] is returning false,
     * until inevitably running out of memory.
     * The default value for high watermark is 2,097,152 bytes.
     */
    public fun writeBufferWatermark(
        numLowBytes: Int,
        numHighBytes: Int,
    ): BootstrapBuilder {
        this.writeBufferWatermarkLow = numLowBytes
        this.writeBufferWatermarkHigh = numHighBytes
        return this
    }

    /**
     * Sets the value of TCP_NODELAY socket option. If [value] is true, Nagle's algorithm will
     * be disabled, which results in smaller delays between writes, but more writes overall.
     * While Nagle's algorithm used to be helpful in the distant past, networking nowadays
     * has come a long way to where this simply degrades the overall performance.
     * The default value is true, meaning Nagle's algorithm is disabled.
     *
     * [Nagle's Algorithm](https://en.wikipedia.org/wiki/Nagle%27s_algorithm)
     *
     * @param value whether to disable TCP_NODELAY optimization.
     */
    public fun tcpNoDelay(value: Boolean): BootstrapBuilder {
        this.tcpNoDelay = value
        return this
    }

    /**
     * Sets the maximum number of TCP connections that can be queued up before the server
     * actually accepts the connection. Note that the default value is whatever the OS' kernel
     * has by default. Increasing it here alone will NOT increase the backlog capacity alone.
     * You must also modify the kernel to allow a higher capacity alongside it.
     *
     * On Linux, backlog can be configured via `somaxconn` and `tcp_max_syn_backlog`.
     * `somaxconn` is the maximum allowed backlow, often defaulting to 128.
     * `tcp_max_syn_backlog` is the maximum allowed half-open connections while the
     * syn-handshake takes place. The default is usually either 1024 or 4096.
     *
     * @param value the maximum number of connections to queue up at the kernel level,
     * limited to the kernel's own configuration. The default on our end is 4096, although
     * most kernels limit it to a smaller value, such as 128. The smallest of the two is picked.
     */
    public fun soBacklog(value: Int): BootstrapBuilder {
        this.soBacklog = value
        return this
    }

    /**
     * Allows for the socket to bind to the same ip & port if the previous socket is in
     * TIME_WAIT state, which happens for a short period after a socket is shut down, to
     * capture any stray packets. The default value is true.
     *
     * @param value whether to enable SO_REUSEADDR.
     */
    public fun reuseAddress(value: Boolean): BootstrapBuilder {
        this.soReuseAddress = value
        return this
    }

    /**
     * Sets a priority array of event loop group types to use, preferring the ones at
     * the front of the array over those at the back. Each type in the array will be
     * tested one by one, until an event loop group is available. Omitting a group type
     * means it will not be used altogether.
     * The default priority order is [EventLoopGroupType.IOURING] -> [EventLoopGroupType.EPOLL] ->
     * [EventLoopGroupType.KQUEUE] -> [EventLoopGroupType.NIO].
     *
     * @param types the event loop group types to try, starting with the front of
     * the array.
     */
    public fun eventLoopGroupTypes(vararg types: EventLoopGroupType): BootstrapBuilder {
        this.eventLoopGroupTypes = types
        return this
    }

    private fun getEventLoopGroupTypes(): Array<out EventLoopGroupType> {
        val types = this.eventLoopGroupTypes
        if (types != null) {
            return types
        }
        return arrayOf(
            IOURING,
            EPOLL,
            KQUEUE,
            NIO,
        )
    }

    private fun determineBossThreadCount(): Int = this.bossThreadCount ?: 1

    private fun determineChildThreadCount(): Int {
        val overwrittenCount = this.childThreadCount
        if (overwrittenCount != null) {
            return overwrittenCount
        }
        // The default value for child thread count is `physicalThreadCount - 2`, with a minimum of 4.
        val cores = Runtime.getRuntime().availableProcessors()
        val physicalThreads = cores * 2
        return max(1, physicalThreads - 2)
    }

    private data class BuildEventLoopGroupsResult(
        val type: EventLoopGroupType,
        val factory: IoHandlerFactory,
        val bossGroup: EventLoopGroup,
        val childGroup: EventLoopGroup,
    )

    private fun buildEventLoopGroups(
        bossThreadCount: Int,
        childThreadCount: Int,
        groupTypes: Array<out EventLoopGroupType>,
    ): BuildEventLoopGroupsResult {
        for (type in groupTypes) {
            try {
                when (type) {
                    IOURING -> {
                        if (!IoUring.isAvailable()) {
                            continue
                        }
                        val factory = IoUringIoHandler.newFactory()
                        val boss = MultiThreadIoEventLoopGroup(bossThreadCount, factory)
                        val child = MultiThreadIoEventLoopGroup(childThreadCount, factory)
                        return BuildEventLoopGroupsResult(type, factory, boss, child)
                    }
                    EPOLL -> {
                        if (!Epoll.isAvailable()) {
                            continue
                        }
                        val factory = EpollIoHandler.newFactory()
                        val boss = MultiThreadIoEventLoopGroup(bossThreadCount, factory)
                        val child = MultiThreadIoEventLoopGroup(childThreadCount, factory)
                        return BuildEventLoopGroupsResult(type, factory, boss, child)
                    }
                    KQUEUE -> {
                        if (!KQueue.isAvailable()) {
                            continue
                        }
                        val factory = KQueueIoHandler.newFactory()
                        val boss = MultiThreadIoEventLoopGroup(bossThreadCount, factory)
                        val child = MultiThreadIoEventLoopGroup(childThreadCount, factory)
                        return BuildEventLoopGroupsResult(type, factory, boss, child)
                    }
                    NIO -> {
                        val factory = NioIoHandler.newFactory()
                        val boss = MultiThreadIoEventLoopGroup(bossThreadCount, factory)
                        val child = MultiThreadIoEventLoopGroup(childThreadCount, factory)
                        return BuildEventLoopGroupsResult(type, factory, boss, child)
                    }
                }
            } catch (t: Throwable) {
                // Notify the user of an error if one does happen, which is possible even if
                // the `isAvailable()` function returns true in some edge cases. This typically
                // means some obscure bug, however, that users might want to look into.
                logger.error(t) {
                    "Unable to create $type event group type."
                }
            }
        }
        throw IllegalStateException("No event loop groups are available in ${groupTypes.contentDeepToString()}")
    }

    private fun determineSocketChannel(type: EventLoopGroupType): Class<out ServerChannel> =
        when (type) {
            IOURING -> IoUringServerSocketChannel::class.java
            EPOLL -> EpollServerSocketChannel::class.java
            KQUEUE -> KQueueServerSocketChannel::class.java
            NIO -> NioServerSocketChannel::class.java
        }

    /**
     * Builds the server bootstrap based on the criteria given through the builder.
     */
    internal fun build(estimator: OutgoingMessageSizeEstimator): ServerBootstrap {
        val bootstrap = ServerBootstrap()
        val groupTypes = getEventLoopGroupTypes()
        val bossThreadCount = determineBossThreadCount()
        val childThreadCount = determineChildThreadCount()
        val (type, factory, bossGroup, childGroup) =
            buildEventLoopGroups(
                bossThreadCount,
                childThreadCount,
                groupTypes,
            )
        val channel = determineSocketChannel(type)
        log {
            "Using IO handler factory: ${factory.javaClass.simpleName} " +
                "(bossThreads: $bossThreadCount, childThreads: $childThreadCount)"
        }
        bootstrap.group(bossGroup, childGroup)
        bootstrap.channel(channel)
        val formatter = NumberFormat.getIntegerInstance()
        val allocator = this.allocator ?: ByteBufAllocator.DEFAULT
        bootstrap.option(ChannelOption.ALLOCATOR, allocator)
        bootstrap.childOption(ChannelOption.ALLOCATOR, allocator)
        log { "Using byte buffer allocator: $allocator" }
        bootstrap.childOption(ChannelOption.AUTO_READ, false)
        log { "Auto read: disabled" }
        val soRcvBufSize = this.soRcvBufSize ?: 65536
        bootstrap.childOption(ChannelOption.SO_RCVBUF, soRcvBufSize)
        log { "Socket receive buffer size: ${formatter.format(soRcvBufSize)}" }
        val soSndBufSize = this.soSndBufSize ?: 65536
        bootstrap.childOption(ChannelOption.SO_SNDBUF, soSndBufSize)
        val soBacklog = soBacklog ?: 4096
        log { "Socket backlog: $soBacklog" }
        bootstrap.option(ChannelOption.SO_BACKLOG, soBacklog)
        val soReuseAddr = soReuseAddress ?: true
        log { "Reuse socket address: $soReuseAddr" }
        bootstrap.option(ChannelOption.SO_REUSEADDR, soReuseAddr)
        log { "Socket send buffer size: ${formatter.format(soSndBufSize)}" }
        val lowWatermark = this.writeBufferWatermarkLow ?: 524_288
        val highWatermark = this.writeBufferWatermarkHigh ?: 2_097_152
        bootstrap.childOption(
            ChannelOption.WRITE_BUFFER_WATER_MARK,
            WriteBufferWaterMark(
                lowWatermark,
                highWatermark,
            ),
        )
        log {
            "Write buffer watermarks: ${formatter.format(lowWatermark)}/${formatter.format(highWatermark)}"
        }
        val tcpNoDelay = this.tcpNoDelay != false
        bootstrap.childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
        log { "Nagle's algorithm (TCP no delay): ${if (tcpNoDelay) "disabled" else "enabled"}" }
        bootstrap.childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, estimator)
        return bootstrap
    }

    private fun log(msg: () -> Any?) {
        logger.debug(msg)
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
