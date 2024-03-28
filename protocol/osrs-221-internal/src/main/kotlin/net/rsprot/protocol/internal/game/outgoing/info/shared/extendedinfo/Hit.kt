package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBarList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMarkList

public class Hit : TransientExtendedInfo() {
    public val headBarList: HeadBarList = HeadBarList()
    public val hitMarkList: HitMarkList = HitMarkList()

    override fun clear() {
        releaseBuffers()
        headBarList.clear()
        hitMarkList.clear()
    }
}
