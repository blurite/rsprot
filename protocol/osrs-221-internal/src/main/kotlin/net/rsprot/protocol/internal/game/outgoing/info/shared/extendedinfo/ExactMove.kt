package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class ExactMove : TransientExtendedInfo() {
    public var deltaX1: UByte = 0u
    public var deltaZ1: UByte = 0u
    public var delay1: UShort = 0u
    public var deltaX2: UByte = 0u
    public var deltaZ2: UByte = 0u
    public var delay2: UShort = 0u
    public var direction: UShort = 0u

    override fun clear() {
        releaseBuffers()
        deltaX1 = 0u
        deltaZ1 = 0u
        delay1 = 0u
        deltaX2 = 0u
        deltaZ2 = 0u
        delay2 = 0u
        direction = 0u
    }
}
