package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class FaceAngle : TransientExtendedInfo() {
    public var angle: Int = -1

    override fun clear() {
        releaseBuffers()
        angle = -1
    }
}
