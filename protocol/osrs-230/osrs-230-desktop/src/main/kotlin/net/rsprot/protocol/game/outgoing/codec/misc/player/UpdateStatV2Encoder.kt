package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStatV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatV2Encoder : MessageEncoder<UpdateStatV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStatV2,
    ) {
        buffer.p4Alt3(message.experience)
        buffer.p1Alt1(message.currentLevel)
        buffer.p1Alt1(message.stat)
        buffer.p1Alt2(message.invisibleBoostedLevel)
    }
}
