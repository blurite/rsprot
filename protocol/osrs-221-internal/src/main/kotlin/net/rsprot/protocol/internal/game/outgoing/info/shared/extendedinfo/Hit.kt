package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBarList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMarkList
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The hit extended info, responsible for tracking all hitmarks and headbars for a given avatar.
 * @param encoders the array of platform-specific encoders for hits.
 */
public class Hit(
    encoders: Array<OnDemandExtendedInfoEncoder<Hit>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<Hit, OnDemandExtendedInfoEncoder<Hit>>(encoders) {
    public val headBarList: HeadBarList = HeadBarList()
    public val hitMarkList: HitMarkList = HitMarkList()

    override fun precompute() {
        // No-op; hits don't get precomputed
    }

    override fun clear() {
        releaseBuffers()
        headBarList.clear()
        hitMarkList.clear()
    }
}
