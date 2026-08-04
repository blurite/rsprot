package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamRotateToCoordinateV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamRotateToCoordinateV3Encoder : MessageEncoder<CamRotateToCoordinateV3> {
    override val prot: ServerProt = GameServerProt.CAM_ROTATETO_COORDINATE_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamRotateToCoordinateV3,
    ) {
        buffer.p2Alt2(message.height)
        buffer.p2Alt2(message.x)
        buffer.p1(message.easing.id)
        buffer.p2(message.cycles)
        buffer.p1(if (message.heightRelative) 1 else 0)
        buffer.p2Alt3(message.z)
        buffer.p1(if (message.trackTarget) 1 else 0)
    }
}
