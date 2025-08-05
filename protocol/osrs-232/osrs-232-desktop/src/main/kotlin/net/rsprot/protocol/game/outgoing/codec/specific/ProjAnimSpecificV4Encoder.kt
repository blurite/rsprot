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
        buffer.p2Alt1(message.startTime)
        buffer.p2Alt2(message.id)
        buffer.p3Alt2(message.sourceIndex)
        buffer.p2(message.endTime)
        buffer.p4Alt2(message.start.packed)
        buffer.p1Alt1(message.angle)
        buffer.p2Alt3(message.endHeight)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2(message.progress)
        buffer.p2Alt1(message.startHeight)
        buffer.p4Alt2(message.end.packed)
    }
}
