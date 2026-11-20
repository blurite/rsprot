package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjDelSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjDelSpecificEncoder : MessageEncoder<ObjDelSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_DEL_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjDelSpecific,
    ) {
        buffer.p2Alt1(message.id)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p4Alt3(message.quantity)
    }
}
