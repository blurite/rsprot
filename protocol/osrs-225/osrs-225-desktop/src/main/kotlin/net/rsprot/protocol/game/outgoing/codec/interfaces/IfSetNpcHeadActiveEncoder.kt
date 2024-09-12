package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetNpcHeadActive
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetNpcHeadActiveEncoder : MessageEncoder<IfSetNpcHeadActive> {
    override val prot: ServerProt = GameServerProt.IF_SETNPCHEAD_ACTIVE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetNpcHeadActive,
    ) {
        buffer.pCombinedIdAlt2(message.combinedId)
        buffer.p2Alt2(message.index)
    }
}
