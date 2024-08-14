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
        buffer.p1Alt2(message.startHeight)
        buffer.p1(message.deltaX)
        buffer.p2Alt1(message.progress)
        buffer.p1Alt1(message.angle)
        buffer.p1Alt3(message.endHeight)
        buffer.p2Alt2(message.endTime)
        buffer.p2(message.id)
        buffer.p3(message.targetIndex)
        buffer.p3Alt1(message.coordInBuildAreaPacked)
        buffer.p1Alt3(message.deltaZ)
        buffer.p2Alt2(message.startTime)
    }
}
