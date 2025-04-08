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
        buffer.p3(message.coordInBuildAreaPacked)
        buffer.p2Alt1(message.endTime)
        buffer.p1Alt3(message.angle)
        buffer.p3Alt1(message.targetIndex)
        buffer.p2Alt2(message.progress)
        buffer.p1Alt3(message.deltaZ)
        buffer.p1Alt3(message.startHeight)
        buffer.p2(message.id)
        buffer.p3Alt2(message.sourceIndex)
        buffer.p2Alt2(message.startTime)
        buffer.p1(message.endHeight)
        buffer.p1Alt1(message.deltaX)
    }
}
