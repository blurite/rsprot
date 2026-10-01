package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark

/**
 * The hitmark extended info, responsible for tracking all hitmarks for a given avatar.
 * @param encoders the array of client-specific encoders for hitmarks.
 */
public class HitmarkList(
    override val encoders: ClientTypeMap<OnDemandExtendedInfoEncoder<HitmarkList>>,
) : TransientExtendedInfo<HitmarkList, OnDemandExtendedInfoEncoder<HitmarkList>>() {
    private val _elements: MutableList<HitMark> = ArrayList()
    public val elements: List<HitMark>
        get() = _elements
    public val size: Int
        get() = _elements.size

    public fun add(hitMark: HitMark) {
        _elements.add(hitMark)
    }

    override fun clear() {
        releaseBuffers()
        _elements.clear()
    }
}
