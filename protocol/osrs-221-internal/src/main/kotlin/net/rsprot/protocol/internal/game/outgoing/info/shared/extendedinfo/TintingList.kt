package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

/**
 * The tinting extended info block.
 * This is a rather special case as tinting is one of the two observer-dependent extended info blocks,
 * along with [Hit]. It is possible for the server to mark tinting for only a single avatar to see.
 * In order to achieve this, we utilize [observerDependent] tinting, indexed by the observer's id.
 * @param encoders the array of platform-specific encoders for tinting.
 */
public class TintingList(
    override val encoders: PlatformMap<OnDemandExtendedInfoEncoder<TintingList>>,
) : TransientExtendedInfo<TintingList, OnDemandExtendedInfoEncoder<TintingList>>() {
    public val global: Tinting = Tinting()
    public val observerDependent: MutableMap<Int, Tinting> = HashMap()

    public operator fun get(index: Int): Tinting {
        return observerDependent.getOrDefault(index, global)
    }

    override fun clear() {
        releaseBuffers()
        global.reset()
        if (observerDependent.isNotEmpty()) {
            observerDependent.clear()
        }
    }
}
