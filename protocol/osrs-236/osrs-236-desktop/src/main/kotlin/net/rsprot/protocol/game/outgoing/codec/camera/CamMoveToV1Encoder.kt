package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToV1Encoder : MessageEncoder<CamMoveToV1> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToV1,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p1(message.rate)
        buffer.p1(message.rate2)
    }
}
