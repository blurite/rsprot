package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ProjAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProjAnimSpecificEncoder : MessageEncoder<ProjAnimSpecific> {
    override val prot: ServerProt = GameServerProt.PROJANIM_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: ProjAnimSpecific,
    ) {
        buffer.p1Alt1(message.angle)
        buffer.p1(message.deltaZ)
        buffer.p1(message.endHeight)
        buffer.p2(message.startTime)
        buffer.p1(message.startHeight)
        buffer.p3Alt3(message.coordInBuildAreaPacked)
        buffer.p1Alt3(message.deltaX)
        buffer.p2Alt3(message.progress)
        buffer.p2(message.endTime)
        buffer.p3Alt1(message.targetIndex)
        buffer.p2Alt3(message.id)
    }
}
