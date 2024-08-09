package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfInitialState
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class IfInitialStateEncoder : MessageEncoder<IfInitialState> {
    override val prot: ServerProt = GameServerProt.IF_INITIALSTATE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfInitialState,
    ) {
        buffer.p2(message.topLevelInterface)
        buffer.p2(message.subInterfaces.size)
        for (subInterface in message.subInterfaces) {
            buffer.p4(subInterface.destinationCombinedId.combinedId)
            buffer.p2(subInterface.interfaceId)
            buffer.p1(subInterface.type)
        }
        for (events in message.events) {
            buffer.p4(events.combinedId.combinedId)
            buffer.p2(events.start)
            buffer.p2(events.end)
            buffer.p4(events.events)
        }
    }
}
