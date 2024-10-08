package net.rsprot.protocol.game.incoming.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.messaging.MessagePublic
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessagePublicDecoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageDecoder<MessagePublic> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PUBLIC

    override fun decode(buffer: JagByteBuf): MessagePublic {
        val type = buffer.g1()
        val colour = buffer.g1()
        val effect = buffer.g1()
        val patternArray =
            if (colour in 13..20) {
                ByteArray(colour - 12) {
                    buffer.g1().toByte()
                }
            } else {
                null
            }
        val huffman = huffmanCodecProvider.provide()
        val hasTrailingByte = type == CLAN_MAIN_CHANNEL_TYPE
        val huffmanSlice =
            if (hasTrailingByte) {
                buffer.buffer.readSlice(buffer.readableBytes() - 1)
            } else {
                buffer.buffer
            }
        val message = huffman.decode(huffmanSlice)
        val clanType =
            if (hasTrailingByte) {
                buffer.g1()
            } else {
                -1
            }
        val pattern =
            if (patternArray != null) {
                MessagePublic.MessageColourPattern(patternArray)
            } else {
                null
            }
        return MessagePublic(
            type,
            colour,
            effect,
            message,
            pattern,
            clanType,
        )
    }

    private companion object {
        private const val CLAN_MAIN_CHANNEL_TYPE: Int = 3
    }
}
