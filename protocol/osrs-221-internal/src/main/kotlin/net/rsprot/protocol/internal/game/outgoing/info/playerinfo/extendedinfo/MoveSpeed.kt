package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class MoveSpeed : TransientExtendedInfo() {
    public var value: Int = -1

    override fun clear() {
        releaseBuffers()
        value = -1
    }
}
