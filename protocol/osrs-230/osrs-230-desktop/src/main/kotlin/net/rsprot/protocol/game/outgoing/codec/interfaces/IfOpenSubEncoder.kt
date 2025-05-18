package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenSub
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfOpenSubEncoder : MessageEncoder<IfOpenSub> {
    override val prot: ServerProt = GameServerProt.IF_OPENSUB

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfOpenSub,
    ) {
        buffer.p1(message.type)
        buffer.p2Alt2(message.interfaceId)
        buffer.pCombinedIdAlt3(message.destinationCombinedId)
    }
}
