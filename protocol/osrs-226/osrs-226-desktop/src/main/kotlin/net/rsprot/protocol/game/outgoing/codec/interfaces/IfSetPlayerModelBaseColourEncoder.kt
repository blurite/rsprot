package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelBaseColour
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetPlayerModelBaseColourEncoder : MessageEncoder<IfSetPlayerModelBaseColour> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_BASECOLOUR

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelBaseColour,
    ) {
        buffer.p1Alt1(message.colour)
        buffer.p1Alt1(message.index)
        buffer.pCombinedIdAlt1(message.combinedId)
    }
}
