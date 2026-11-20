package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToArcV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamMoveToArcV3Encoder : MessageEncoder<CamMoveToArcV3> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_ARC_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToArcV3,
    ) {
        buffer.p2Alt3(message.cycles)
        buffer.p2(message.height)
        buffer.p2(message.centerZ)
        buffer.p1Alt1(if (message.heightRelative) 1 else 0)
        buffer.p2(message.centerX)
        buffer.p1(message.easing.id)
        buffer.p2Alt3(message.destinationX)
        buffer.p2Alt3(message.destinationZ)
        buffer.p1Alt1(if (message.ignoreTerrain) 1 else 0)
    }
}
