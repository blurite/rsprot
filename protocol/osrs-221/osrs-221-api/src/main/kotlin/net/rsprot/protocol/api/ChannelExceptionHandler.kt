package net.rsprot.protocol.api

import io.netty.channel.ChannelHandlerContext

/**
 * The channel exception handler is an interface that is invoked whenever Netty catches
 * an exception in the channels. The server is expected to close the connection in any such case,
 * if it is still open, and log the exception behind it.
 */
public fun interface ChannelExceptionHandler {
    /**
     * Invoked whenever a Netty handler catches an exception.
     * @param ctx the channel handler context behind this connection
     * @param cause the causation behind the exception
     */
    public fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    )
}
