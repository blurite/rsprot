package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetAnim
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetAnimEncoder : MessageEncoder<IfSetAnim> {
    override val prot: ServerProt = GameServerProt.IF_SETANIM

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetAnim,
    ) {
        buffer.p2Alt3(message.anim)
        buffer.pCombinedIdAlt1(message.combinedId)
    }
}
