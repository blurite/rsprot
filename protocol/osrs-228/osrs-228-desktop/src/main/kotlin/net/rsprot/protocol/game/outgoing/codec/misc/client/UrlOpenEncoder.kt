package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.UrlOpen
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UrlOpenEncoder : MessageEncoder<UrlOpen> {
    override val prot: ServerProt = GameServerProt.URL_OPEN

    override val encryptedPayload: Boolean
        get() = true

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UrlOpen,
    ) {
        val marker = buffer.writerIndex()
        buffer.pjstr(message.url)

        // Encrypt the entire buffer with a stream cipher
        for (i in marker..<buffer.writerIndex()) {
            buffer.buffer.setByte(i, buffer.buffer.getByte(i) + streamCipher.nextInt())
        }
    }
}
