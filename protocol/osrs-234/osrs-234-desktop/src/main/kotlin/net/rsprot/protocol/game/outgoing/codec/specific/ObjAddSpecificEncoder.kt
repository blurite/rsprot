package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjAddSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjAddSpecificEncoder : MessageEncoder<ObjAddSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_ADD_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjAddSpecific,
    ) {
        // The function at the bottom of the OBJ_ADD_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_add(world, level, x, z, id, quantity, opFlags,
        // timeUntilPublic, timeUntilDespawn, ownershipType, neverBecomesPublic);
        buffer.p2(message.id)
        buffer.p1(if (message.neverBecomesPublic) 1 else 0)
        buffer.p1Alt1(message.ownershipType)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p2Alt3(message.timeUntilDespawn)
        buffer.p4Alt1(message.quantity)
        buffer.p2(message.timeUntilPublic)
        buffer.p1Alt2(message.opFlags.toInt())
    }
}
