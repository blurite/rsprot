package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamRotateTo
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamRotateToEncoder : MessageEncoder<CamRotateTo> {
    override val prot: ServerProt = GameServerProt.CAM_ROTATETO

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamRotateTo,
    ) {
        buffer.p2(message.yaw)
        buffer.p2(message.pitch)
        buffer.p2(message.cycles)
        buffer.p1(message.easing.id)
    }
}
