package net.rsprot.protocol.game.outgoing.codec.varp

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpSync
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarpSyncEncoder : MessageEncoder<VarpSync> {
    override val prot: ServerProt = GameServerProt.VARP_SYNC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: VarpSync,
    ) {
    }
}
