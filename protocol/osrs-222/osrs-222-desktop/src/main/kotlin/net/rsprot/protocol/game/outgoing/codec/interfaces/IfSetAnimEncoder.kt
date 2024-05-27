package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetAnim
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetAnimEncoder : MessageEncoder<IfSetAnim> {
    override val prot: ServerProt = GameServerProt.IF_SETANIM

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetAnim,
    ) {
        buffer.pCombinedIdAlt2(message.combinedId)
        buffer.p2Alt3(message.anim)
    }
}
