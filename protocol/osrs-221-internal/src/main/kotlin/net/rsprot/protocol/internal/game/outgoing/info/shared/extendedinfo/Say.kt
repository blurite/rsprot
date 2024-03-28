package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class Say : TransientExtendedInfo() {
    public var text: String? = null

    override fun clear() {
        releaseBuffers()
        text = null
    }
}
