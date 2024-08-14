package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt1

public class IfSetEventsEncoder : MessageEncoder<IfSetEvents> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEvents,
    ) {
        buffer.p2Alt2(message.end)
        buffer.p2Alt3(message.start)
        buffer.p4Alt1(message.events)
        buffer.pCombinedIdAlt1(message.combinedId)
    }
}
