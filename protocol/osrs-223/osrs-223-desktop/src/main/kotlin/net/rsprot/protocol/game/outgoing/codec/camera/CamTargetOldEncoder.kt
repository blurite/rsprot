package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTargetOld
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamTargetOldEncoder : MessageEncoder<CamTargetOld> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET_OLD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: CamTargetOld,
    ) {
        when (val type = message.type) {
            is CamTargetOld.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p2(type.index)
            }
            is CamTargetOld.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p2(type.index)
            }
            is CamTargetOld.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p2(type.index)
            }
        }
    }
}
