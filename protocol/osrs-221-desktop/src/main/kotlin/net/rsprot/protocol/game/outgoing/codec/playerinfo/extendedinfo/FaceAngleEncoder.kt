package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.outgoing.info.extendedinfo.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle

public class FaceAngleEncoder : ExtendedInfoEncoder<FaceAngle> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedPlayerIndex: Int,
        extendedInfo: FaceAngle,
    ) {
        buffer.p2(extendedInfo.angle)
    }
}
