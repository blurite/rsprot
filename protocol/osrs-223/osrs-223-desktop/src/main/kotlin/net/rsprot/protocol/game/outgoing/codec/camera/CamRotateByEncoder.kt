package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamRotateBy
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamRotateByEncoder : MessageEncoder<CamRotateBy> {
    override val prot: ServerProt = GameServerProt.CAM_ROTATEBY

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamRotateBy,
    ) {
        buffer.p2(message.yAngle)
        buffer.p2(message.xAngle)
        buffer.p2(message.duration)
        buffer.p1(message.function.id)
    }
}
