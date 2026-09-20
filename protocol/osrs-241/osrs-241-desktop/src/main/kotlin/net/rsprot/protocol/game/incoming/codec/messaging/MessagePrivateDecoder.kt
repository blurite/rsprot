package net.rsprot.protocol.game.incoming.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.messaging.MessagePrivate
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessagePrivateDecoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageDecoder<MessagePrivate> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun decode(buffer: JagByteBuf): MessagePrivate {
        val name = buffer.gjstr()
        val huffman = huffmanCodecProvider.provide()
        val message = huffman.decode(buffer)
        return MessagePrivate(
            name,
            message,
        )
    }
}
