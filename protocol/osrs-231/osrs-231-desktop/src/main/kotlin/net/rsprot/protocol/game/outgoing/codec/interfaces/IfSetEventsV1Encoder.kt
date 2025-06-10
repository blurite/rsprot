package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEventsV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetEventsV1Encoder : MessageEncoder<IfSetEventsV1> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEventsV1,
    ) {
        buffer.p2(message.start)
        buffer.pCombinedIdAlt2(message.combinedId)
        buffer.p2(message.end)
        buffer.p4(message.events)
    }
}
