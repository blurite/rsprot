package net.rsprot.protocol.common.js5.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultFileRegion
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.js5.outgoing.prot.Js5ServerProt
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import net.rsprot.protocol.message.codec.MessageEncoder
import kotlin.math.min

public class Js5GroupResponseEncoder : MessageEncoder<Js5GroupResponse> {
    override val prot: ServerProt = Js5ServerProt.JS5_GROUP_RESPONSE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: Js5GroupResponse,
    ) {
        val marker = buffer.writerIndex()
        when (val response = message.response) {
            is Js5GroupResponse.Js5ByteBufGroupResponse -> {
                val offset = response.offset
                val limit = response.limit
                val buf = response.content()
                buffer.buffer.writeBytes(
                    buf,
                    offset,
                    limit,
                )
            }
            is Js5GroupResponse.Js5FileGroupResponse -> {
                if (message.response.key != 0) {
                    throw IllegalStateException("FileRegion responses do not support XOR encryption.")
                }
                val raf = response.file
                val offset = response.offset
                val limit = response.limit
                val fileRegion =
                    DefaultFileRegion(
                        raf.channel,
                        offset.toLong(),
                        min(limit - offset, BLOCK_LENGTH).toLong(),
                    )
                ctx.write(fileRegion, ctx.voidPromise())
                return
            }
        }
        // Encrypt the entire payload if XOR key is provided.
        // Note that this might not be safe to do if the `out` buffer is a composite buffer consisting
        // of multiple backing buffers, need to look into that problem further.
        if (message.response.key != 0) {
            val out = buffer.buffer
            for (i in marker..<buffer.writerIndex()) {
                out.setByte(i, out.getByte(i).toInt() xor message.response.key)
            }
        }
    }

    private companion object {
        private const val BLOCK_LENGTH = 512
    }
}
