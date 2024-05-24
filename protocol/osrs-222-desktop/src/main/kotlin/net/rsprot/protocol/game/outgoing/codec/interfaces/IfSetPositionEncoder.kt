package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPosition
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPositionEncoder : MessageEncoder<IfSetPosition> {
    override val prot: ServerProt = GameServerProt.IF_SETPOSITION

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPosition,
    ) {
        buffer.p4Alt2(message.combinedId.combinedId)
        buffer.p2(message.x)
        buffer.p2Alt2(message.y)
    }
}
