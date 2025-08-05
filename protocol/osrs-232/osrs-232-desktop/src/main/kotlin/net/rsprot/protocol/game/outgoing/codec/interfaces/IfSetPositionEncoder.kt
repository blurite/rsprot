package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPosition
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetPositionEncoder : MessageEncoder<IfSetPosition> {
    override val prot: ServerProt = GameServerProt.IF_SETPOSITION

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPosition,
    ) {
        buffer.p2(message.y)
        buffer.pCombinedId(message.combinedId)
        buffer.p2Alt2(message.x)
    }
}
