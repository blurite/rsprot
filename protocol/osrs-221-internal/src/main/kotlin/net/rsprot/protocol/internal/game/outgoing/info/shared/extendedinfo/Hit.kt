package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBarList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMarkList
import net.rsprot.protocol.shared.platform.PlatformType

public class Hit(
    encoders: Array<OnDemandExtendedInfoEncoder<Hit>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<Hit, OnDemandExtendedInfoEncoder<Hit>>(encoders) {
    public val headBarList: HeadBarList = HeadBarList()
    public val hitMarkList: HitMarkList = HitMarkList()

    override fun clear() {
        releaseBuffers()
        headBarList.clear()
        hitMarkList.clear()
    }
}
