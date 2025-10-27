package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjEnabledOpsSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjEnabledOpsSpecificEncoder : MessageEncoder<ObjEnabledOpsSpecific> {
    override val prot: ServerProt = GameServerProt.OBJ_ENABLED_OPS_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjEnabledOpsSpecific,
    ) {
        // The function at the bottom of the OBJ_ENABLED_OPS_SPECIFIC has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_enabledops(world, level, x, z, id, opFlags)
        buffer.p1(message.opFlags.toInt())
        buffer.p4Alt3(message.coordGrid.packed)
        buffer.p2Alt3(message.id)
    }
}
