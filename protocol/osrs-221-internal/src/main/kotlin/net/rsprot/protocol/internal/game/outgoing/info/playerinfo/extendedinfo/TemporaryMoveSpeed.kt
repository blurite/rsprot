package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class TemporaryMoveSpeed(
    encoders: Array<PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<TemporaryMoveSpeed, PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>>(encoders) {
    public var value: Int = -1

    override fun clear() {
        releaseBuffers()
        value = -1
    }
}
