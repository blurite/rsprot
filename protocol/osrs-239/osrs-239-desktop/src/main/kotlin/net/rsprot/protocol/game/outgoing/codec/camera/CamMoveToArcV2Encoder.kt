package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToArcV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamMoveToArcV2Encoder : MessageEncoder<CamMoveToArcV2> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_ARC_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToArcV2,
    ) {
        buffer.p2Alt3(message.cycles)
        buffer.p2Alt2(message.destinationZ)
        buffer.p2Alt2(message.centerZ)
        buffer.p2(message.height)
        buffer.p1Alt2(message.easing.id)
        buffer.p2Alt2(message.destinationX)
        buffer.p1Alt3(if (message.ignoreTerrain) 1 else 0)
        buffer.p2Alt1(message.centerX)
    }
}
