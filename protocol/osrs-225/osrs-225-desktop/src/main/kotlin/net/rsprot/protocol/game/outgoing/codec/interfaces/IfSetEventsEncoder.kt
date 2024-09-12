package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt3

public class IfSetEventsEncoder : MessageEncoder<IfSetEvents> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEvents,
    ) {
        buffer.p2(message.start)
        buffer.p2Alt1(message.end)
        buffer.pCombinedIdAlt3(message.combinedId)
        buffer.p4(message.events)
    }
}
