package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo.Chat

public class PlayerChatEncoder : PrecomputedExtendedInfoEncoder<Chat> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Chat,
    ): JagByteBuf {
        val codec = huffmanCodecProvider.provide()
        val text = extendedInfo.text ?: ""
        val colour = extendedInfo.colour.toInt()
        val patternLength = if (colour in 13..20) colour - 12 else 0
        val capacity = 5 + text.length + patternLength
        val buffer =
            alloc
                .buffer(capacity)
                .toJagByteBuf()
        buffer.p2(colour shl 8 or extendedInfo.effects.toInt())
        buffer.p1Alt2(extendedInfo.modicon.toInt())
        buffer.p1Alt1(if (extendedInfo.autotyper) 1 else 0)
        val huffmanBuffer =
            alloc
                .buffer(text.length)
                .toJagByteBuf()
        codec.encode(huffmanBuffer, text)
        buffer.p1Alt3(huffmanBuffer.readableBytes())
        try {
            buffer.pdataAlt3(huffmanBuffer.buffer)
        } finally {
            huffmanBuffer.buffer.release()
        }
        if (patternLength in 1..8) {
            val pattern = checkNotNull(extendedInfo.pattern)
            for (i in 0..<patternLength) {
                buffer.p1Alt2(pattern[i].toInt())
            }
        }
        return buffer
    }
}
