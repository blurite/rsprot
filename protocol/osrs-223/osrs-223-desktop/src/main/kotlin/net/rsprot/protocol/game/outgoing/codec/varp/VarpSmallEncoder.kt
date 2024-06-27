package net.rsprot.protocol.game.outgoing.codec.varp

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpSmall
import net.rsprot.protocol.message.codec.MessageEncoder

public class VarpSmallEncoder : MessageEncoder<VarpSmall> {
    override val prot: ServerProt = GameServerProt.VARP_SMALL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: VarpSmall,
    ) {
        buffer.p1Alt3(message.value)
        buffer.p2Alt3(message.id)
    }
}
