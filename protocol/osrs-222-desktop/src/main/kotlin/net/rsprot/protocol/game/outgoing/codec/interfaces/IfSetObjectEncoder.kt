package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetObject
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetObjectEncoder : MessageEncoder<IfSetObject> {
    override val prot: ServerProt = GameServerProt.IF_SETOBJECT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetObject,
    ) {
        buffer.p4Alt3(message.count)
        buffer.p4(message.combinedId.combinedId)
        buffer.p2Alt1(message.obj)
    }
}
