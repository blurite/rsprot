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
        buffer.p1(message.angle)
        buffer.p3Alt2(message.targetIndex)
        buffer.p1Alt1(message.deltaZ)
        buffer.p2Alt1(message.endTime)
        buffer.p1Alt2(message.endHeight)
        buffer.p1Alt3(message.deltaX)
        buffer.p1Alt3(message.startHeight)
        buffer.p2Alt1(message.id)
        buffer.p2Alt3(message.progress)
        buffer.p2Alt2(message.startTime)
        buffer.p3(message.coordInBuildAreaPacked)
    }
}
