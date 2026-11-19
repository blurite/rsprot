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
        buffer.p1Alt1(if (message.neverBecomesPublic) 1 else 0)
        buffer.p2(message.timeUntilPublic)
        buffer.p4Alt1(message.coordGrid.packed)
        buffer.p2Alt1(message.timeUntilDespawn)
        buffer.p1Alt3(message.ownershipType)
        buffer.p2Alt3(message.id)
        buffer.p1Alt3(message.opFlags.toInt())
        buffer.p4(message.quantity)
    }
}
