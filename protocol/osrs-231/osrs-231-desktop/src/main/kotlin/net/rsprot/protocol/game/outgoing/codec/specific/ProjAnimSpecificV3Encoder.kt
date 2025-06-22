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
        buffer.p1Alt3(message.deltaX)
        buffer.p2Alt3(message.endTime)
        buffer.p2Alt3(message.id)
        buffer.p2(message.startTime)
        buffer.p3Alt2(message.targetIndex)
        buffer.p1Alt3(message.deltaZ)
        buffer.p1Alt3(message.endHeight)
        buffer.p1Alt1(message.startHeight)
        buffer.p3(message.sourceIndex)
        buffer.p2Alt3(message.progress)
        buffer.p3Alt3(message.coordInBuildAreaPacked)
        buffer.p1Alt1(message.angle)
    }
}
