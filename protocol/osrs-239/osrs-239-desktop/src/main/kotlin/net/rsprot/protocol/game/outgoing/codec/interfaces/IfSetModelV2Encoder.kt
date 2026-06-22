package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetModelV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetModelV2Encoder : MessageEncoder<IfSetModelV2> {
    override val prot: ServerProt = GameServerProt.IF_SETMODEL_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetModelV2,
    ) {
        buffer.pCombinedIdAlt2(message.combinedId)
        buffer.p4Alt2(message.model)
    }
}
