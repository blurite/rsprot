package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ProjAnimSpecificV3
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProjAnimSpecificV3Encoder : MessageEncoder<ProjAnimSpecificV3> {
    override val prot: ServerProt = GameServerProt.PROJANIM_SPECIFIC_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ProjAnimSpecificV3,
    ) {
        buffer.p3Alt1(message.coordInBuildAreaPacked)
        buffer.p1(message.deltaZ)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2Alt1(message.progress)
        buffer.p1Alt3(message.angle)
        buffer.p1Alt2(message.endHeight)
        buffer.p3Alt1(message.sourceIndex)
        buffer.p2Alt2(message.startTime)
        buffer.p2Alt1(message.id)
        buffer.p1(message.deltaX)
        buffer.p2Alt1(message.endTime)
        buffer.p1Alt1(message.startHeight)
    }
}
