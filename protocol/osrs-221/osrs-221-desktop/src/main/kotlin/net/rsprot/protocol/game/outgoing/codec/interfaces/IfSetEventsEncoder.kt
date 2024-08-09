package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetEventsEncoder : MessageEncoder<IfSetEvents> {
    override val prot: ServerProt = GameServerProt.IF_SETEVENTS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetEvents,
    ) {
        buffer.p2(message.end)
        buffer.p2Alt3(message.start)
        buffer.p4Alt3(message.combinedId.combinedId)
        buffer.p4Alt2(message.events)
    }
}
