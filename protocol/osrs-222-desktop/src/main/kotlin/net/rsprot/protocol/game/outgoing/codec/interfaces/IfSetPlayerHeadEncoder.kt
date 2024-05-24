package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerHead
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerHeadEncoder : MessageEncoder<IfSetPlayerHead> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERHEAD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPlayerHead,
    ) {
        buffer.p4Alt2(message.combinedId.combinedId)
    }
}
