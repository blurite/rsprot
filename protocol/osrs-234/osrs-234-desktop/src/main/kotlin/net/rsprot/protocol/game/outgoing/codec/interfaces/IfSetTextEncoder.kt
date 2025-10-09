package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetText
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetTextEncoder : MessageEncoder<IfSetText> {
    override val prot: ServerProt = GameServerProt.IF_SETTEXT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetText,
    ) {
        buffer.pjstr(message.text)
        buffer.pCombinedId(message.combinedId)
    }
}
