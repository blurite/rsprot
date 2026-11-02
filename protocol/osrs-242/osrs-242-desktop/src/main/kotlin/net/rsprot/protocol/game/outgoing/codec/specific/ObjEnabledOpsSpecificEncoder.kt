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
        buffer.p2Alt2(message.id)
        buffer.p1Alt1(message.opFlags.toInt())
        buffer.p4Alt1(message.coordGrid.packed)
    }
}
