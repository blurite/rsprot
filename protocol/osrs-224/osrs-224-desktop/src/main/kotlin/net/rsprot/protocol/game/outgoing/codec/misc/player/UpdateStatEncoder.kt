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
        buffer.p1Alt3(message.stat)
        buffer.p1(message.invisibleBoostedLevel)
        buffer.p4Alt2(message.experience)
        buffer.p1Alt3(message.currentLevel)
    }
}
