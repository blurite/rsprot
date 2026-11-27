package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ObjCustomiseSpecificV2
import net.rsprot.protocol.message.codec.MessageEncoder

public class ObjCustomiseSpecificV2Encoder : MessageEncoder<ObjCustomiseSpecificV2> {
    override val prot: ServerProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjCustomiseSpecificV2,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p4(message.coordGrid.packed)
        buffer.p1(message.recolours.size)
        for ((index, value) in message.recolours) {
            buffer.p1Alt1(index)
            buffer.p2(value)
        }
        val colour = message.colour
        buffer.p1(if (colour != null) 1 else 0)
        if (colour != null) {
            buffer.p2Alt1(colour)
        }
        buffer.p2Alt3(message.model)
        buffer.p4(message.quantity)
        buffer.p1(message.retextures.size)
        for ((index, value) in message.retextures) {
            buffer.p1Alt3(index)
            buffer.p2(value)
        }
    }
}
