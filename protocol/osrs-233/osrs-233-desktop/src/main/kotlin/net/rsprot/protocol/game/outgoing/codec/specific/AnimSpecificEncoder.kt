package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.AnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class AnimSpecificEncoder : MessageEncoder<AnimSpecific> {
    override val prot: ServerProt = GameServerProt.ANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: AnimSpecific,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p1Alt2(message.delay)
    }
}
