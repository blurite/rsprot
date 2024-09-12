package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.LocAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class LocAnimSpecificEncoder : MessageEncoder<LocAnimSpecific> {
    override val prot: ServerProt = GameServerProt.LOC_ANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: LocAnimSpecific,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p3(message.coordInBuildAreaPacked)
        buffer.p1Alt1(message.locPropertiesPacked)
    }
}
