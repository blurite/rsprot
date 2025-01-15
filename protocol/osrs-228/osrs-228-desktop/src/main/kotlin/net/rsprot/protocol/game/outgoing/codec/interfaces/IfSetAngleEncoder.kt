package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetAngle
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetAngleEncoder : MessageEncoder<IfSetAngle> {
    override val prot: ServerProt = GameServerProt.IF_SETANGLE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetAngle,
    ) {
        buffer.p2Alt2(message.angleY)
        buffer.p2Alt3(message.angleX)
        buffer.p2Alt3(message.zoom)
        buffer.pCombinedId(message.combinedId)
    }
}
