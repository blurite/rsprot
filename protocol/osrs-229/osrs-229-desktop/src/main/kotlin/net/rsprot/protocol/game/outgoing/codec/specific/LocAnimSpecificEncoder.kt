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
        buffer.p3Alt2(message.coordInBuildAreaPacked)
        buffer.p1Alt3(message.locPropertiesPacked)
        buffer.p2Alt3(message.id)
    }
}
