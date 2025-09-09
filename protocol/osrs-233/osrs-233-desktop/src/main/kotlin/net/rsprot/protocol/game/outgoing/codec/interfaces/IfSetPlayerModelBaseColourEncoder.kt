package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelBaseColour
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetPlayerModelBaseColourEncoder : MessageEncoder<IfSetPlayerModelBaseColour> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_BASECOLOUR

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelBaseColour,
    ) {
        buffer.p1Alt2(message.colour)
        buffer.pCombinedIdAlt3(message.combinedId)
        buffer.p1(message.index)
    }
}
