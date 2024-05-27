package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfClearInv
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfClearInvEncoder : MessageEncoder<IfClearInv> {
    override val prot: ServerProt = GameServerProt.IF_CLEARINV

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfClearInv,
    ) {
        buffer.pCombinedIdAlt3(message.combinedId)
    }
}
