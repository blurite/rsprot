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
        buffer.p2(message.yaw)
        buffer.p2(message.pitch)
        buffer.p2(message.cycles)
        buffer.p1(message.easing.id)
    }
}
