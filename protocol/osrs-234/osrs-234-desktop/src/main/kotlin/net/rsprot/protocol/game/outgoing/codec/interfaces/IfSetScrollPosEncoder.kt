package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetScrollPos
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetScrollPosEncoder : MessageEncoder<IfSetScrollPos> {
    override val prot: ServerProt = GameServerProt.IF_SETSCROLLPOS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetScrollPos,
    ) {
        buffer.p2(message.scrollPos)
        buffer.pCombinedIdAlt2(message.combinedId)
    }
}
