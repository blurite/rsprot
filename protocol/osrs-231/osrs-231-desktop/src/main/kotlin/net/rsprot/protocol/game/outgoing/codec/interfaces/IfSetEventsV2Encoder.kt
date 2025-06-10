package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEventsV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetEventsV2Encoder : MessageEncoder<IfSetEventsV2> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEventsV2,
    ) {
        buffer.p4Alt3(message.events1)
        buffer.p4Alt3(message.events2)
        buffer.p2(message.end)
        buffer.pCombinedId(message.combinedId)
        buffer.p2(message.start)
    }
}
