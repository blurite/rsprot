package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.HintArrow
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class HintArrowEncoder : MessageEncoder<HintArrow> {
    override val prot: ServerProt = GameServerProt.HINT_ARROW

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: HintArrow,
    ) {
        when (val type = message.type) {
            is HintArrow.NpcHintArrow -> {
                buffer.p1(1)
                buffer.p2(type.index)
                buffer.skipWrite(3)
            }
            is HintArrow.PlayerHintArrow -> {
                buffer.p1(10)
                buffer.p2(type.index)
                buffer.skipWrite(3)
            }
            is HintArrow.TileHintArrow -> {
                buffer.p1(type.positionId)
                buffer.p2(type.x)
                buffer.p2(type.z)
                buffer.p1(type.height)
            }
            HintArrow.ResetHintArrow -> {
                buffer.p1(0)
                buffer.skipWrite(5)
            }
        }
    }
}
