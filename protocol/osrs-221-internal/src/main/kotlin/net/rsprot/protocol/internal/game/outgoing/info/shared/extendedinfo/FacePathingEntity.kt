package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class FacePathingEntity(
    encoders: Array<PrecomputedExtendedInfoEncoder<FacePathingEntity>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<FacePathingEntity, PrecomputedExtendedInfoEncoder<FacePathingEntity>>(encoders) {
    public var index: Int = DEFAULT_VALUE

    override fun clear() {
        releaseBuffers()
        index = DEFAULT_VALUE
    }

    public companion object {
        public const val DEFAULT_VALUE: Int = -1
    }
}
