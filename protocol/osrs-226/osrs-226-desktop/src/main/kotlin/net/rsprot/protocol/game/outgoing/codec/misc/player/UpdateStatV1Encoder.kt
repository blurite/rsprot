package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStatV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatV1Encoder : MessageEncoder<UpdateStatV1> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStatV1,
    ) {
        buffer.p1(message.currentLevel)
        buffer.p4Alt3(message.experience)
        buffer.p1Alt3(message.stat)
    }
}
