package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.HeatmapToggle
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class HeatmapToggleEncoder : MessageEncoder<HeatmapToggle> {
    override val prot: ServerProt = GameServerProt.HEATMAP_TOGGLE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: HeatmapToggle,
    ) {
        buffer.pboolean(message.enabled)
    }
}
