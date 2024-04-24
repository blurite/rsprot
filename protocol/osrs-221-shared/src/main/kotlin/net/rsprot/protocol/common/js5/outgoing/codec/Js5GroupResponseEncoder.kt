package net.rsprot.protocol.common.js5.outgoing.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultFileRegion
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.common.js5.outgoing.prot.Js5ServerProt
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import net.rsprot.protocol.message.codec.MessageEncoder
import java.io.RandomAccessFile
import kotlin.math.min

public class Js5GroupResponseEncoder : MessageEncoder<Js5GroupResponse> {
    override val prot: ServerProt = Js5ServerProt.JS5_GROUP_RESPONSE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: Js5GroupResponse,
    ) {
        val key: Int =
            ctx
                .channel()
                .attr(ChannelAttributes.XOR_ENCRYPTION_KEY)
                .get() ?: 0
        val marker = buffer.writerIndex()
        when (val response = message.response) {
            is Js5GroupResponse.PreparedJs5ByteBufGroupResponse -> {
                val buf = response.content()
                buffer.buffer.writeBytes(buf, buf.readerIndex(), buf.readableBytes())
            }
            is Js5GroupResponse.PreparedJs5FileGroupResponse -> {
                if (key != 0) {
                    throw IllegalStateException("FileRegion responses do not support XOR encryption.")
                }
                val raf = RandomAccessFile(response.file, "r")
                val fileRegion = DefaultFileRegion(raf.channel, 0, raf.length())
                ctx.write(fileRegion, ctx.voidPromise())
            }
            is Js5GroupResponse.UnpreparedJs5ByteBufGroupResponse -> {
                pUnpreparedJs5ByteBufResponse(
                    response.request,
                    response.content(),
                    buffer,
                )
            }
        }
        // Encrypt the entire payload if XOR key is provided.
        if (key != 0) {
            val out = buffer.buffer
            for (i in marker..<buffer.writerIndex()) {
                out.setByte(i, out.getByte(i).toInt() xor key)
            }
        }
    }

    private fun pUnpreparedJs5ByteBufResponse(
        request: Js5GroupRequest,
        response: ByteBuf,
        buffer: JagByteBuf,
    ) {
        buffer.p1(request.archiveId)
        buffer.p2(request.groupId)
        val out = buffer.buffer
        val readableBytes = response.readableBytes()
        // Block length - 3 as we already wrote 3 bytes at the start
        val len = min(readableBytes, BLOCK_LENGTH - 3)
        out.writeBytes(response, 0, len)
        var offset = len
        while (offset < readableBytes) {
            out.writeByte(0xFF)
            // Block length - 1 as we already wrote the separator 0xFF
            val nextBlockLength = min(readableBytes - offset, BLOCK_LENGTH - 1)
            out.writeBytes(response, offset, nextBlockLength)
            offset += nextBlockLength
        }
    }

    private companion object {
        private const val BLOCK_LENGTH = 512
    }
}
