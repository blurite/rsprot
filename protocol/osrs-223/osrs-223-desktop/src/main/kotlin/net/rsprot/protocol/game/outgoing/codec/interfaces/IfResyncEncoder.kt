package net.rsprot.protocol.game.outgoing.codec.interfaces

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfResync
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.pCombinedId

@Consistent
public class IfResyncEncoder : MessageEncoder<IfResync> {
    override val prot: ServerProt = GameServerProt.IF_RESYNC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: IfResync,
    ) {
        buffer.p2(message.topLevelInterface)
        buffer.p2(message.subInterfaces.size)
        for (subInterface in message.subInterfaces) {
            buffer.pCombinedId(subInterface.destinationCombinedId)
            buffer.p2(subInterface.interfaceId)
            buffer.p1(subInterface.type)
        }
        for (events in message.events) {
            buffer.pCombinedId(events.combinedId)
            buffer.p2(events.start)
            buffer.p2(events.end)
            buffer.p4(events.events)
        }
    }
}
