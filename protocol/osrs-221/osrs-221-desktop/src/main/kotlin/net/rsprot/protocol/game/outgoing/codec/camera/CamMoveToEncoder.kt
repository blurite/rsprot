package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveTo
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamMoveToEncoder : MessageEncoder<CamMoveTo> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveTo,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p1(message.speed)
        buffer.p1(message.acceleration)
    }
}
