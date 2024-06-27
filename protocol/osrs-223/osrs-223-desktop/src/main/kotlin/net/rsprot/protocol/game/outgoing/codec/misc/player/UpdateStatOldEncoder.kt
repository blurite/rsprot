package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStatOld
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatOldEncoder : MessageEncoder<UpdateStatOld> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT_OLD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateStatOld,
    ) {
        buffer.p4(message.experience)
        buffer.p1Alt3(message.currentLevel)
        buffer.p1Alt3(message.stat)
    }
}
