package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToEased
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToEasedEncoder : MessageEncoder<CamMoveToEased> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_EASED

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamMoveToEased,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p2(message.duration)
        buffer.pboolean(!message.maintainFixedAltitude)
        buffer.p1(message.function.id)
    }
}
