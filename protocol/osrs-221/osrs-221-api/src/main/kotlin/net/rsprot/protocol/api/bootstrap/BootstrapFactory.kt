package net.rsprot.protocol.api.bootstrap

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
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
import net.rsprot.protocol.api.logging.networkLog

/**
 * A bootstrap factory responsible for generating the Netty bootstrap
 */
public class BootstrapFactory(
    private val alloc: ByteBufAllocator,
) {
    /**
     * Creates an IO handler factory based on the best available event loop group.
     */
    public fun createIoHandlerFactory(): IoHandlerFactory =
        when {
            IoUring.isAvailable() -> IoUringIoHandler.newFactory()
            Epoll.isAvailable() -> EpollIoHandler.newFactory()
            KQueue.isAvailable() -> KQueueIoHandler.newFactory()
            else -> NioIoHandler.newFactory()
        }

    /**
     * Creates a parent loop group with a single thread behind it, based on the best
     * available event loop group.
     */
    public fun createParentLoopGroup(nThreads: Int = 1): EventLoopGroup =
        MultiThreadIoEventLoopGroup(nThreads, createIoHandlerFactory())

    /**
     * Creates a child loop group with a number of threads based on availableProcessors * 2,
     * which is done at Netty level.
     */
    public fun createChildLoopGroup(nThreads: Int = 0): EventLoopGroup =
        MultiThreadIoEventLoopGroup(nThreads, createIoHandlerFactory())

    /**
     * Creates a server bootstrap using the parent and child event loop groups with
     * a configuration that closely resembles the values found in the client.
     */
    public fun createServerBootstrap(
        parentGroup: EventLoopGroup,
        childGroup: EventLoopGroup,
    ): ServerBootstrap {
        val channel =
            when {
                IoUring.isAvailable() -> IoUringServerSocketChannel::class.java
                Epoll.isAvailable() -> EpollServerSocketChannel::class.java
                KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
                else -> NioServerSocketChannel::class.java
            }
        networkLog(logger) {
            "Bootstrap event loop group: ${parentGroup.javaClass.simpleName}"
        }
        return ServerBootstrap()
            .group(parentGroup, childGroup)
            .channel(channel)
            .option(ChannelOption.ALLOCATOR, alloc)
            .option(ChannelOption.SO_BACKLOG, 4096)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.ALLOCATOR, alloc)
            .childOption(ChannelOption.AUTO_READ, false)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_RCVBUF, 65536)
            .childOption(ChannelOption.SO_SNDBUF, 65536)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(524_288, 2_097_152))
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
