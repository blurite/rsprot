package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamShake
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamShakeEncoder : MessageEncoder<CamShake> {
    override val prot: ServerProt = GameServerProt.CAM_SHAKE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamShake,
    ) {
        buffer.p1(message.axis)
        buffer.p1(message.random)
        buffer.p1(message.amplitude)
        buffer.p1(message.rate)
    }
}
