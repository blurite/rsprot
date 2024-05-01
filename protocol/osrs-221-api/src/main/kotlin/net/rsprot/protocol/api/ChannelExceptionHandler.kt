package net.rsprot.protocol.api

import io.netty.channel.ChannelHandlerContext

public fun interface ChannelExceptionHandler {
    public fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    )
}
