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
        // The function at the bottom of the OBJ_DEL_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_del(world, level, x, z, id, quantity)
        buffer.p4Alt1(message.quantity)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p2(message.id)
    }
}
