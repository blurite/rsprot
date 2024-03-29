package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class FaceAngle(
    encoders: Array<PrecomputedExtendedInfoEncoder<FaceAngle>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<FaceAngle, PrecomputedExtendedInfoEncoder<FaceAngle>>(encoders) {
    public var angle: Int = -1

    override fun clear() {
        releaseBuffers()
        angle = -1
    }
}
