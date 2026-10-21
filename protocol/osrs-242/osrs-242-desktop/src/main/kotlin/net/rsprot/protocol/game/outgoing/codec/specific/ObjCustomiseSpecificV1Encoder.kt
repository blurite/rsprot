package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjCustomiseSpecificV1
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjCustomiseSpecificV1Encoder : MessageEncoder<ObjCustomiseSpecificV1> {
    override val prot: ServerProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjCustomiseSpecificV1,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE_SPECIFIC_V1 has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objCustomise(world, level, x, z, id, count, recol, recolIndex, retex, retexIndex, model);
        buffer.p2Alt1(message.id)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p4Alt3(message.quantity)
        buffer.p2Alt3(message.recol)
        buffer.p2Alt3(message.retex)
        buffer.p2Alt2(message.retexIndex)
        buffer.p2Alt3(message.recolIndex)
        buffer.p2Alt2(message.model)
    }
}
