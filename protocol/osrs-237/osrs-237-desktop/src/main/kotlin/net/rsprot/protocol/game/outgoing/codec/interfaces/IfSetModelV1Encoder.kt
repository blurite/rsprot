package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetModelV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetModelV1Encoder : MessageEncoder<IfSetModelV1> {
    override val prot: ServerProt = GameServerProt.IF_SETMODEL_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetModelV1,
    ) {
        buffer.p2Alt3(message.model)
        buffer.pCombinedIdAlt2(message.combinedId)
    }
}
