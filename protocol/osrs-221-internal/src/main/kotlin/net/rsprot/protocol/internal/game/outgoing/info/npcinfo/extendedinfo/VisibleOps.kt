package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class VisibleOps(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<VisibleOps>>,
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
