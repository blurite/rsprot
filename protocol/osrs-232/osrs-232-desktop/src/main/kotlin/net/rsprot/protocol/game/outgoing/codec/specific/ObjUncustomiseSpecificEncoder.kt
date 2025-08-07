package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjUncustomiseSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjUncustomiseSpecificEncoder : MessageEncoder<ObjUncustomiseSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_UNCUSTOMISE_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjUncustomiseSpecific,
    ) {
        // The function at the bottom of the OBJ_UNCUSTOMISE_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objUncustomise(world, level, x, z, id, count);
        buffer.p2Alt3(message.id)
        buffer.p4Alt3(message.coordGrid.packed)
        buffer.p4Alt2(message.quantity)
    }
}
