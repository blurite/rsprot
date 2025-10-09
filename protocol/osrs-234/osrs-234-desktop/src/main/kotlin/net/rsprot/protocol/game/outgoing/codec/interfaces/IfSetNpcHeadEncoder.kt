package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetNpcHead
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetNpcHeadEncoder : MessageEncoder<IfSetNpcHead> {
    override val prot: ServerProt = GameServerProt.IF_SETNPCHEAD

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetNpcHead,
    ) {
        buffer.pCombinedIdAlt3(message.combinedId)
        buffer.p2Alt1(message.npc)
    }
}
