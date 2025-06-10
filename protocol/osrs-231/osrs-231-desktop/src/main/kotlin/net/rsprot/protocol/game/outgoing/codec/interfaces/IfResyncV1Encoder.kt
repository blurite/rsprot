package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfResyncV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.pCombinedId

@Consistent
public class IfResyncV1Encoder : MessageEncoder<IfResyncV1> {
    override val prot: ServerProt = GameServerProt.IF_RESYNC_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfResyncV1,
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
