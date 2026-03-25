package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetModelV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetModelV2Encoder : MessageEncoder<IfSetModelV2> {
    override val prot: ServerProt = GameServerProt.IF_SETMODEL_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetModelV2,
    ) {
        buffer.p4Alt1(message.model)
        buffer.pCombinedIdAlt3(message.combinedId)
    }
}
