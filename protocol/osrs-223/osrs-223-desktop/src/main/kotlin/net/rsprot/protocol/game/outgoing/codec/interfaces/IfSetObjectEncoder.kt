package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetObject
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetObjectEncoder : MessageEncoder<IfSetObject> {
    override val prot: ServerProt = GameServerProt.IF_SETOBJECT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetObject,
    ) {
        buffer.p4(message.count)
        buffer.p2Alt3(message.obj)
        buffer.pCombinedId(message.combinedId)
    }
}
