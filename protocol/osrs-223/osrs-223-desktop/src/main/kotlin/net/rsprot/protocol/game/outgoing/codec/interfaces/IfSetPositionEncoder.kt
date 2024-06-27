package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPosition
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetPositionEncoder : MessageEncoder<IfSetPosition> {
    override val prot: ServerProt = GameServerProt.IF_SETPOSITION

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPosition,
    ) {
        buffer.p2Alt2(message.y)
        buffer.pCombinedIdAlt1(message.combinedId)
        buffer.p2Alt3(message.x)
    }
}
