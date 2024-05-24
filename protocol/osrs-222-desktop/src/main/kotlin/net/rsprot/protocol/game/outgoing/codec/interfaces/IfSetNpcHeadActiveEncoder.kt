package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetNpcHeadActive
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetNpcHeadActiveEncoder : MessageEncoder<IfSetNpcHeadActive> {
    override val prot: ServerProt = GameServerProt.IF_SETNPCHEAD_ACTIVE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetNpcHeadActive,
    ) {
        buffer.p2Alt2(message.index)
        buffer.p4(message.combinedId.combinedId)
    }
}
