package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEventsV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetEventsV2Encoder : MessageEncoder<IfSetEventsV2> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEventsV2,
    ) {
        // The function uses arguments in this order:
        // component, start, end, events1, events2
        buffer.p4Alt2(message.events1)
        buffer.p2(message.end)
        buffer.p2Alt2(message.start)
        buffer.pCombinedIdAlt1(message.combinedId)
        buffer.p4Alt3(message.events2)
    }
}
