package net.rsprot.protocol.game.outgoing.codec.varp

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpReset
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarpResetEncoder : MessageEncoder<VarpReset> {
    override val prot: ServerProt = GameServerProt.VARP_RESET

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: VarpReset,
    ) {
    }
}
