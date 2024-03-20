package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf

/**
 * Reads the remaining contents of the [ByteBuf] into a byte array.
 * This function will increase [ByteBuf.writerIndex] by [ByteBuf.readableBytes]
 */
public fun ByteBuf.toByteArray(): ByteArray {
    val array = ByteArray(readableBytes())
    readBytes(array)
    return array
}
