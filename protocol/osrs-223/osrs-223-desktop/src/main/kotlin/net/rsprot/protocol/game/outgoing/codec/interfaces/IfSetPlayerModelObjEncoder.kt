package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelObj
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetPlayerModelObjEncoder : MessageEncoder<IfSetPlayerModelObj> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPlayerModelObj,
    ) {
        buffer.p4Alt1(message.obj)
        buffer.pCombinedIdAlt3(message.combinedId)
    }
}
