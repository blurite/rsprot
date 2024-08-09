package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToEasedCircular
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToEasedCircularEncoder : MessageEncoder<CamMoveToEasedCircular> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_EASED_CIRCULAR

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToEasedCircular,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p1(message.centerXInBuildArea)
        buffer.p1(message.centerZInBuildArea)
        buffer.p2(message.duration)
        buffer.pboolean(!message.maintainFixedAltitude)
        buffer.p1(message.function.id)
    }
}
