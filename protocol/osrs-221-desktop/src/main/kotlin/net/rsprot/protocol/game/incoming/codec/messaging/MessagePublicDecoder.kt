package net.rsprot.protocol.game.incoming.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.messaging.MessagePublicMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class MessagePublicDecoder : MessageDecoder<MessagePublicMessage> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PUBLIC

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessagePublicMessage {
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
        val message = tools.huffmanCodec.decode(buffer)
        val clanType =
            if (type == CLAN_MAIN_CHANNEL_TYPE) {
                buffer.g1()
            } else {
                -1
            }
        val pattern =
            if (patternArray != null) {
                MessagePublicMessage.MessageColourPattern(patternArray)
            } else {
                null
            }
        return MessagePublicMessage(
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
