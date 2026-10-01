package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class HeadIconCustomisation(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>,
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
