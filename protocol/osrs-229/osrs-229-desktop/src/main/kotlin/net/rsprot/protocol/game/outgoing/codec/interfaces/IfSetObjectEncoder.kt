package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetObject
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetObjectEncoder : MessageEncoder<IfSetObject> {
    override val prot: ServerProt = GameServerProt.IF_SETOBJECT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetObject,
    ) {
        buffer.p4Alt3(message.count)
        buffer.p2Alt3(message.obj)
        buffer.pCombinedIdAlt1(message.combinedId)
    }
}
