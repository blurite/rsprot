package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class FacePathingEntity : TransientExtendedInfo() {
    public var index: Int = 0

    override fun clear() {
        releaseBuffers()
        index = 0
    }
}
