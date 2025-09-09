package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelSelf
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetPlayerModelSelfEncoder : MessageEncoder<IfSetPlayerModelSelf> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_SELF

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelSelf,
    ) {
        // The boolean is inverted client-sided, it's more of a "skip copying"
        buffer.p1(if (message.copyObjs) 0 else 1)
        buffer.pCombinedIdAlt2(message.combinedId)
    }
}
