package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetModel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetModelEncoder : MessageEncoder<IfSetModel> {
    override val prot: ServerProt = GameServerProt.IF_SETMODEL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetModel,
    ) {
        buffer.p4Alt2(message.combinedId.combinedId)
        buffer.p2Alt2(message.model)
    }
}
