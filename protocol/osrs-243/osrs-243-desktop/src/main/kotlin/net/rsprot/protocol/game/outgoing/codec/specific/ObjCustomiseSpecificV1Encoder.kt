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
        buffer.p2Alt1(message.recolIndex)
        buffer.p2(message.retexIndex)
        buffer.p4(message.coordGrid.packed)
        buffer.p2Alt1(message.model)
        buffer.p4Alt2(message.quantity)
        buffer.p2Alt2(message.id)
        buffer.p2Alt3(message.retex)
        buffer.p2(message.recol)
    }
}
