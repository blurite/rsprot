package net.rsprot.protocol.handler.bootstrap

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollMode
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.incubator.channel.uring.IOUring
import io.netty.incubator.channel.uring.IOUringEventLoopGroup
import io.netty.incubator.channel.uring.IOUringServerSocketChannel

public class BootstrapFactory(
    private val alloc: ByteBufAllocator,
) {
    public fun createEventLoopGroup(): EventLoopGroup {
        return when {
            IOUring.isAvailable() -> IOUringEventLoopGroup()
            Epoll.isAvailable() -> EpollEventLoopGroup()
            KQueue.isAvailable() -> KQueueEventLoopGroup()
            else -> NioEventLoopGroup()
        }
    }

    public fun createServerBootstrap(group: EventLoopGroup): ServerBootstrap {
        val channel =
            when (group) {
                is IOUringEventLoopGroup -> IOUringServerSocketChannel::class.java
                is EpollEventLoopGroup -> EpollServerSocketChannel::class.java
                is KQueueEventLoopGroup -> KQueueServerSocketChannel::class.java
                is NioEventLoopGroup -> NioServerSocketChannel::class.java
                else -> throw IllegalArgumentException("Unknown EventLoopGroup type")
            }

        return ServerBootstrap()
            .group(group)
            .channel(channel)
            .option(ChannelOption.ALLOCATOR, alloc)
            .childOption(ChannelOption.ALLOCATOR, alloc)
            .childOption(ChannelOption.AUTO_READ, false)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_RCVBUF, 65536)
            .childOption(ChannelOption.SO_SNDBUF, 65536)
            .childOption(ChannelOption.SO_TIMEOUT, 30_000)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(524_288, 2_097_152))
            .also {
                if (group is EpollEventLoopGroup) {
                    it.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                }
            }
    }
}
