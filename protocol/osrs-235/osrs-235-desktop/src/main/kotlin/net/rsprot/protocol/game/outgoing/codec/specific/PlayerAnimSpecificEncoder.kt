package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.PlayerAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class PlayerAnimSpecificEncoder : MessageEncoder<PlayerAnimSpecific> {
    override val prot: ServerProt = GameServerProt.PLAYER_ANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: PlayerAnimSpecific,
    ) {
        buffer.p1Alt3(message.delay)
        buffer.p2Alt1(message.id)
    }
}
