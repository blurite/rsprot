package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjCountSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjCountSpecificEncoder : MessageEncoder<ObjCountSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_COUNT_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjCountSpecific,
    ) {
        // The function at the bottom of the OBJ_COUNT_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_count(world, level, x, z, id, oldQuantity, newQuantity)
        buffer.p2(message.id)
        buffer.p4Alt1(message.oldQuantity)
        buffer.p4(message.coordGrid.packed)
        buffer.p4Alt3(message.newQuantity)
    }
}
