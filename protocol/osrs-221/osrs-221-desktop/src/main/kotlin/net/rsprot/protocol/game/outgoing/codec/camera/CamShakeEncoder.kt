package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamShake
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamShakeEncoder : MessageEncoder<CamShake> {
    override val prot: ServerProt = GameServerProt.CAM_SHAKE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamShake,
    ) {
        buffer.p1(message.type)
        buffer.p1(message.randomAmount)
        buffer.p1(message.sineAmount)
        buffer.p1(message.sineFrequency)
    }
}
