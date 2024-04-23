package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.platform.PlatformMap

public class HeadIconCustomisation(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>,
) : TransientExtendedInfo<HeadIconCustomisation, PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>() {
    public var flag: Int = DEFAULT_FLAG
    public val headIconGroups: IntArray =
        IntArray(8) {
            -1
        }
    public val headIconIndices: ShortArray =
        ShortArray(8) {
            -1
        }

    override fun clear() {
        releaseBuffers()
        flag = DEFAULT_FLAG
        headIconGroups.fill(-1)
        headIconIndices.fill(-1)
    }

    public companion object {
        public const val DEFAULT_FLAG: Int = 0
    }
}
