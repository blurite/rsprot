package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfMoveSub
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfMoveSubEncoder : MessageEncoder<IfMoveSub> {
    override val prot: ServerProt = GameServerProt.IF_MOVESUB

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfMoveSub,
    ) {
        buffer.p4(message.sourceCombinedId.combinedId)
        buffer.p4Alt1(message.destinationCombinedId.combinedId)
    }
}
