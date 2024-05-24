package net.rsprot.protocol.game.outgoing.codec.camera

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.OculusSync
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class OculusSyncEncoder : MessageEncoder<OculusSync> {
    override val prot: ServerProt = GameServerProt.OCULUS_SYNC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: OculusSync,
    ) {
        buffer.p4(message.value)
    }
}
