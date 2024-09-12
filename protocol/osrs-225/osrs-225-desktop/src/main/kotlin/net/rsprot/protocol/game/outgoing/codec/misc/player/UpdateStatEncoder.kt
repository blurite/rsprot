package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStat
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatEncoder : MessageEncoder<UpdateStat> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStat,
    ) {
        buffer.p4Alt3(message.experience)
        buffer.p1Alt3(message.stat)
        buffer.p1Alt2(message.invisibleBoostedLevel)
        buffer.p1Alt1(message.currentLevel)
    }
}
