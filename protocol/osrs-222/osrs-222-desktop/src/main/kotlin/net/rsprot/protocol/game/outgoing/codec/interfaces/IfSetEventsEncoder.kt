package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetEventsEncoder : MessageEncoder<IfSetEvents> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfSetEvents,
    ) {
        buffer.p2Alt2(message.start)
        buffer.p2Alt1(message.end)
        buffer.p4Alt2(message.events)
        buffer.p4Alt1(message.combinedId.combinedId)
    }
}
