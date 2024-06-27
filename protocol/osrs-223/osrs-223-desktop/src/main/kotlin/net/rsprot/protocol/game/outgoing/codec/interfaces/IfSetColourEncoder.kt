package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetColour
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetColourEncoder : MessageEncoder<IfSetColour> {
    override val prot: ServerProt = GameServerProt.IF_SETCOLOUR

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetColour,
    ) {
        buffer.pCombinedIdAlt3(message.combinedId)
        buffer.p2Alt1(message.colour15BitPacked)
    }
}
