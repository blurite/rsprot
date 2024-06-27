package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelObj
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetPlayerModelObjEncoder : MessageEncoder<IfSetPlayerModelObj> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPlayerModelObj,
    ) {
        buffer.pCombinedIdAlt1(message.combinedId)
        buffer.p4Alt3(message.obj)
    }
}
