package net.rsprot.protocol.game.outgoing.codec.varp

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpLarge
import net.rsprot.protocol.message.codec.MessageEncoder

public class VarpLargeEncoder : MessageEncoder<VarpLarge> {
    override val prot: ServerProt = GameServerProt.VARP_LARGE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: VarpLarge,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p4Alt1(message.value)
    }
}
