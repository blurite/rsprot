package net.rsprot.protocol.game.outgoing.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.MessagePrivateEcho
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessagePrivateEchoEncoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageEncoder<MessagePrivateEcho> {
    override val prot: ServerProt = GameServerProt.MESSAGE_PRIVATE_ECHO

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MessagePrivateEcho,
    ) {
        buffer.pjstr(message.recipient)
        val huffman = huffmanCodecProvider.provide()
        huffman.encode(buffer, message.message)
    }
}
