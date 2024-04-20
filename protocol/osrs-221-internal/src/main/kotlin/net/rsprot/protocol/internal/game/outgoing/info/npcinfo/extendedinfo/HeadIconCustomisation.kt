package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class HeadIconCustomisation(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>,
) : TransientExtendedInfo<HeadIconCustomisation, PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>() {
    public var flag: Int = 0
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
        flag = 0
        headIconGroups.fill(-1)
        headIconIndices.fill(-1)
    }
}
