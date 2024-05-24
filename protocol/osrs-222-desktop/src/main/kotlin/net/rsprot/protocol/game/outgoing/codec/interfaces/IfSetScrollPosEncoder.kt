package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetScrollPos
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetScrollPosEncoder : MessageEncoder<IfSetScrollPos> {
    override val prot: ServerProt = GameServerProt.IF_SETSCROLLPOS

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetScrollPos,
    ) {
        buffer.p2Alt1(message.scrollPos)
        buffer.p4(message.combinedId.combinedId)
    }
}
