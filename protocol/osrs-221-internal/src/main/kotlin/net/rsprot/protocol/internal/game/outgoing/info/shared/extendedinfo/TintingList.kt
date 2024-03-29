package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class TintingList(
    encoders: Array<OnDemandExtendedInfoEncoder<TintingList>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<TintingList, OnDemandExtendedInfoEncoder<TintingList>>(encoders) {
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
