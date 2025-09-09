package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjCustomiseSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjCustomiseSpecificEncoder : MessageEncoder<ObjCustomiseSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjCustomiseSpecific,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objCustomise(world, level, x, z, id, count, recol, recolIndex, retex, retexIndex, model);
        buffer.p2(message.recolIndex)
        buffer.p2Alt2(message.retexIndex)
        buffer.p2Alt3(message.model)
        buffer.p4Alt3(message.quantity)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p2Alt1(message.id)
        buffer.p2Alt1(message.recol)
        buffer.p2Alt3(message.retex)
    }
}
