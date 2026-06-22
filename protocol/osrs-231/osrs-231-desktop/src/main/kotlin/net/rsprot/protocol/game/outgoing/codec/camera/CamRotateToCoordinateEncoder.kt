package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamRotateToCoordinate
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamRotateToCoordinateEncoder : MessageEncoder<CamRotateToCoordinate> {
    override val prot: ServerProt = GameServerProt.CAM_ROTATETO_COORDINATE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamRotateToCoordinate,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p2(message.cycles)
        buffer.p1(message.easing.id)
    }
}
