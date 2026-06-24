package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamRotateToCoordinateV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamRotateToCoordinateV2Encoder : MessageEncoder<CamRotateToCoordinateV2> {
    override val prot: ServerProt = GameServerProt.CAM_ROTATETO_COORDINATE_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamRotateToCoordinateV2,
    ) {
        buffer.p2Alt1(message.height)
        buffer.p2Alt3(message.z)
        buffer.p2Alt2(message.x)
        buffer.p1Alt3(message.easing.id)
        buffer.p2(message.cycles)
    }
}
