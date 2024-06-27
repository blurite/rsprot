package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenSub
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfOpenSubEncoder : MessageEncoder<IfOpenSub> {
    override val prot: ServerProt = GameServerProt.IF_OPENSUB

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfOpenSub,
    ) {
        buffer.p1Alt3(message.type)
        buffer.p2Alt1(message.interfaceId)
        buffer.pCombinedId(message.destinationCombinedId)
    }
}
