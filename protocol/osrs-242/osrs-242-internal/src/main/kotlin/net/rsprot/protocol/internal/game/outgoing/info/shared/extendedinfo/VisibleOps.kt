package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class VisibleOps(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<VisibleOps>>,
) : TransientExtendedInfo<VisibleOps, PrecomputedExtendedInfoEncoder<VisibleOps>>() {
    public var ops: UByte = DEFAULT_OPS

    override fun clear() {
        releaseBuffers()
        ops = DEFAULT_OPS
    }

    public companion object {
        public const val DEFAULT_OPS: UByte = 0b11111u
    }
}
