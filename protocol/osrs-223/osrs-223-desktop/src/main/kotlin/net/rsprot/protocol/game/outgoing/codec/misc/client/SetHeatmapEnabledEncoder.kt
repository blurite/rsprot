package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.SetHeatmapEnabled
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetHeatmapEnabledEncoder : MessageEncoder<SetHeatmapEnabled> {
    override val prot: ServerProt = GameServerProt.SET_HEATMAP_ENABLED

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: SetHeatmapEnabled,
    ) {
        buffer.pboolean(message.enabled)
    }
}
