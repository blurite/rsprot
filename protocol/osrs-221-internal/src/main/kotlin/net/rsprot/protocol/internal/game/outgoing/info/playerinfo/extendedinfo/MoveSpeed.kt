package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class MoveSpeed(
    encoders: Array<PrecomputedExtendedInfoEncoder<MoveSpeed>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<MoveSpeed, PrecomputedExtendedInfoEncoder<MoveSpeed>>(encoders) {
    public var value: Int = DEFAULT_MOVESPEED

    override fun clear() {
        releaseBuffers()
        value = DEFAULT_MOVESPEED
    }

    public companion object {
        public const val DEFAULT_MOVESPEED: Int = 0
    }
}
