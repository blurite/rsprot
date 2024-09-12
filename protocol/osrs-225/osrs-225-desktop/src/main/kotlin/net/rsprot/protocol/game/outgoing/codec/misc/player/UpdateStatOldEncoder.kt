package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStatOld
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatOldEncoder : MessageEncoder<UpdateStatOld> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT_OLD

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStatOld,
    ) {
        buffer.p4Alt2(message.experience)
        buffer.p1Alt2(message.currentLevel)
        buffer.p1(message.stat)
    }
}
