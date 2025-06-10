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
        buffer.p1(message.stat)
        buffer.p4Alt2(message.experience)
        buffer.p1Alt2(message.invisibleBoostedLevel)
        buffer.p1(message.currentLevel)
    }
}
