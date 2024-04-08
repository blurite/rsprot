package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamSmoothReset
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamSmoothResetEncoder : MessageEncoder<CamSmoothReset> {
    override val prot: ServerProt = GameServerProt.CAM_SMOOTHRESET

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamSmoothReset,
    ) {
        buffer.p1(message.cameraMoveConstantSpeed)
        buffer.p1(message.cameraMoveProportionalSpeed)
        buffer.p1(message.cameraLookConstantSpeed)
        buffer.p1(message.cameraLookProportionalSpeed)
    }
}
