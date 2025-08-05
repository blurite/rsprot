package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ProjAnimSpecificV4
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProjAnimSpecificV4Encoder : MessageEncoder<ProjAnimSpecificV4> {
    override val prot: ServerProt = GameServerProt.PROJANIM_SPECIFIC_V4

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ProjAnimSpecificV4,
    ) {
        buffer.p4Alt1(message.start.packed)
        buffer.p3Alt1(message.targetIndex)
        buffer.p2Alt1(message.startHeight)
        buffer.p4Alt3(message.end.packed)
        buffer.p1Alt3(message.angle)
        buffer.p3Alt1(message.sourceIndex)
        buffer.p2Alt1(message.startTime)
        buffer.p2Alt1(message.id)
        buffer.p2(message.endHeight)
        buffer.p2Alt1(message.endTime)
        buffer.p2Alt1(message.progress)
    }
}
