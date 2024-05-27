package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelBodyType
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerModelBodyTypeEncoder : MessageEncoder<IfSetPlayerModelBodyType> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_BODYTYPE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetPlayerModelBodyType,
    ) {
        buffer.p4Alt2(message.combinedId.combinedId)
        buffer.p1Alt1(message.bodyType)
    }
}
