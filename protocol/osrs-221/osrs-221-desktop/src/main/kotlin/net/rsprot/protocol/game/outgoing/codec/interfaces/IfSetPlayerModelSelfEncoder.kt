package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelSelf
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerModelSelfEncoder : MessageEncoder<IfSetPlayerModelSelf> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_SELF

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPlayerModelSelf,
    ) {
        // The boolean is inverted client-sided, it's more of a "skip copying"
        buffer.p1(if (message.copyObjs) 0 else 1)
        buffer.p4Alt3(message.combinedId.combinedId)
    }
}
