package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtEasedAngleRelative
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamLookAtEasedAngleRelativeEncoder : MessageEncoder<CamLookAtEasedAngleRelative> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_EASED_ANGLE_RELATIVE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtEasedAngleRelative,
    ) {
        buffer.p2(message.yAngle)
        buffer.p2(message.xAngle)
        buffer.p2(message.duration)
        buffer.p1(message.function.id)
    }
}
