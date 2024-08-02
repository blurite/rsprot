package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToArc
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToArc : MessageEncoder<CamMoveToArc> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_ARC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamMoveToArc,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p1(message.centerXInBuildArea)
        buffer.p1(message.centerZInBuildArea)
        buffer.p2(message.cycles)
        buffer.pboolean(message.ignoreTerrain)
        buffer.p1(message.easing.id)
    }
}
