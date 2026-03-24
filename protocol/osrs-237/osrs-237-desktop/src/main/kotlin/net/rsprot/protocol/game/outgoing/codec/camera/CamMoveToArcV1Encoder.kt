package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToArcV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToArcV1Encoder : MessageEncoder<CamMoveToArcV1> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_ARC_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToArcV1,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p1(message.centerXInBuildArea)
        buffer.p1(message.centerZInBuildArea)
        buffer.p2(message.cycles)
        buffer.pboolean(message.ignoreTerrain)
        buffer.p1(message.easing.id)
    }
}
