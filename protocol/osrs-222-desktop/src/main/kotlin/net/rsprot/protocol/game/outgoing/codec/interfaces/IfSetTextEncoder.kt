package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetText
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetTextEncoder : MessageEncoder<IfSetText> {
    override val prot: ServerProt = GameServerProt.IF_SETTEXT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetText,
    ) {
        buffer.pjstr(message.text)
        buffer.p4Alt3(message.combinedId.combinedId)
    }
}
