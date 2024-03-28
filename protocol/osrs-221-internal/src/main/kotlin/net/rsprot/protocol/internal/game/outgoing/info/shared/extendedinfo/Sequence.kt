package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class Sequence : TransientExtendedInfo() {
    public var id: UShort = 0xFFFFu
    public var delay: UShort = 0u

    override fun clear() {
        releaseBuffers()
        id = 0xFFFFu
        delay = 0u
    }
}
