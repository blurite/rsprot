package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelBaseColour
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerModelBaseColourEncoder : MessageEncoder<IfSetPlayerModelBaseColour> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_BASECOLOUR

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelBaseColour,
    ) {
        buffer.p4(message.combinedId.combinedId)
        buffer.p1Alt2(message.colour)
        buffer.p1Alt2(message.index)
    }
}
