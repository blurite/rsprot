package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.PlayerSpotAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class PlayerSpotAnimSpecificEncoder : MessageEncoder<PlayerSpotAnimSpecific> {
    override val prot: ServerProt = GameServerProt.PLAYER_SPOTANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: PlayerSpotAnimSpecific,
    ) {
        buffer.p2Alt1(message.id)
        buffer.p4((message.height shl 16) or message.delay)
        buffer.p1Alt1(message.slot)
        buffer.p2Alt2(message.index)
    }
}
