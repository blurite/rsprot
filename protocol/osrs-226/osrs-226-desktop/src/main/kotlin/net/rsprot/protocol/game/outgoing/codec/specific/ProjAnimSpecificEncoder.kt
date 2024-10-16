package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.ProjAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProjAnimSpecificEncoder : MessageEncoder<ProjAnimSpecific> {
    override val prot: ServerProt = GameServerProt.PROJANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ProjAnimSpecific,
    ) {
        buffer.p1Alt1(message.angle)
        buffer.p1Alt1(message.deltaZ)
        buffer.p1(message.deltaX)
        buffer.p3Alt2(message.sourceIndex)
        buffer.p2(message.progress)
        buffer.p2Alt1(message.endTime)
        buffer.p1Alt1(message.endHeight)
        buffer.p3Alt1(message.targetIndex)
        buffer.p2Alt1(message.id)
        buffer.p1(message.startHeight)
        buffer.p3(message.coordInBuildAreaPacked)
        buffer.p2Alt3(message.startTime)
    }
}
