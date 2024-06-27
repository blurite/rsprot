package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetHide
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetHideEncoder : MessageEncoder<IfSetHide> {
    override val prot: ServerProt = GameServerProt.IF_SETHIDE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetHide,
    ) {
        buffer.pCombinedIdAlt3(message.combinedId)
        buffer.p1(if (message.hidden) 1 else 0)
    }
}
