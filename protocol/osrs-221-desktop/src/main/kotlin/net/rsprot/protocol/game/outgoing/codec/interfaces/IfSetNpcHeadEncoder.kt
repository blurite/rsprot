package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetNpcHead
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetNpcHeadEncoder : MessageEncoder<IfSetNpcHead> {
    override val prot: ServerProt = GameServerProt.IF_SETNPCHEAD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetNpcHead,
    ) {
        buffer.p2Alt2(message.npc)
        buffer.p4(message.combinedId.combinedId)
    }
}
