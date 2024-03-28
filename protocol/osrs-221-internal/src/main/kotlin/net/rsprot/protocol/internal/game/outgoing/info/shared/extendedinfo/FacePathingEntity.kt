package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class FacePathingEntity : TransientExtendedInfo() {
    public var index: Int = DEFAULT_VALUE

    override fun clear() {
        releaseBuffers()
        index = DEFAULT_VALUE
    }

    public companion object {
        public const val DEFAULT_VALUE: Int = -1
    }
}
