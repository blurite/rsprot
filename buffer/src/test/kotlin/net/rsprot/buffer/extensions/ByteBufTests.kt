package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

internal fun buffer(): ByteBuf {
    return Unpooled.buffer()
}
