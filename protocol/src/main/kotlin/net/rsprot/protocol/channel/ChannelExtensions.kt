@file:JvmName("ChannelExtensions")

package net.rsprot.protocol.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import io.netty.util.AttributeKey
import net.rsprot.protocol.binary.BinaryBlob
import net.rsprot.protocol.binary.BinaryHeader
import net.rsprot.protocol.binary.BinaryStream
import org.jire.netty.haproxy.HAProxyAttributes.sourceHost

/**
 * Gets the host address from the given channel.
 */
public fun Channel.hostAddress(): String = sourceHost

/**
 * Gets the host address from the given channel handler context.
 */
public fun ChannelHandlerContext.hostAddress(): String = sourceHost

/**
 * Replaces a channel handler with a new variant.
 */
public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler =
    replace(T::class.java, newHandler::class.qualifiedName, newHandler)

private val binaryBlobKey: AttributeKey<BinaryBlob> =
    AttributeKey.newInstance("binary_blob")
private val binaryHeaderBuilderKey: AttributeKey<BinaryHeader.Builder> =
    AttributeKey.newInstance("binary_header_builder")

public fun Channel.binaryHeaderBuilderOrNull(): BinaryHeader.Builder? {
    return attr(binaryHeaderBuilderKey)?.get()
}

public fun Channel.setBinaryHeaderBuilder(builder: BinaryHeader.Builder?) {
    attr(binaryHeaderBuilderKey)?.set(builder)
}

public fun Channel.binaryHeaderOrNull(): BinaryHeader? {
    return getBinaryBlobOrNull()?.header
}

public fun Channel.binaryStreamOrNull(): BinaryStream? {
    return getBinaryBlobOrNull()?.stream
}

public fun Channel.setBinaryBlob(blob: BinaryBlob?) {
    attr(binaryBlobKey)?.set(blob)
}

public fun Channel.getBinaryBlobOrNull(): BinaryBlob? {
    return attr(binaryBlobKey)?.get()
}
