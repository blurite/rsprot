package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetModel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetModelEncoder : MessageEncoder<IfSetModel> {
    override val prot: ServerProt = GameServerProt.IF_SETMODEL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetModel,
    ) {
        buffer.pCombinedIdAlt1(message.combinedId)
        buffer.p2Alt1(message.model)
    }
}
