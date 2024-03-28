package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class MoveSpeed : TransientExtendedInfo() {
    public var value: Int = DEFAULT_MOVESPEED

    override fun clear() {
        releaseBuffers()
        value = DEFAULT_MOVESPEED
    }

    public companion object {
        public const val DEFAULT_MOVESPEED: Int = 0
    }
}
