package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar

/**
 * The headbar extended info, responsible for tracking all headbars for a given avatar.
 * @param encoders the array of client-specific encoders for headbars.
 */
public class HeadbarList(
    override val encoders: ClientTypeMap<OnDemandExtendedInfoEncoder<HeadbarList>>,
) : TransientExtendedInfo<HeadbarList, OnDemandExtendedInfoEncoder<HeadbarList>>() {
    private val _elements: MutableList<HeadBar> = ArrayList()
    public val elements: List<HeadBar>
        get() = _elements
    public val size: Int
        get() = _elements.size

    public fun add(headbar: HeadBar) {
        _elements.add(headbar)
    }

    override fun clear() {
        releaseBuffers()
        _elements.clear()
    }
}
