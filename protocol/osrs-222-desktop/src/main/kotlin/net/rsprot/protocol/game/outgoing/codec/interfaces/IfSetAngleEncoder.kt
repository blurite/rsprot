package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetAngle
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetAngleEncoder : MessageEncoder<IfSetAngle> {
    override val prot: ServerProt = GameServerProt.IF_SETANGLE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetAngle,
    ) {
        buffer.p2Alt1(message.zoom)
        buffer.p2Alt3(message.angleY)
        buffer.p2(message.angleX)
        buffer.p4Alt2(message.combinedId.combinedId)
    }
}
