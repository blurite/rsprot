package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStat
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateStatEncoder : MessageEncoder<UpdateStat> {
    override val prot: ServerProt = GameServerProt.UPDATE_STAT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateStat,
    ) {
        buffer.p1Alt1(message.stat)
        buffer.p1Alt1(message.currentLevel)
        buffer.p1Alt3(message.invisibleBoostedLevel)
        buffer.p4Alt1(message.experience)
    }
}
