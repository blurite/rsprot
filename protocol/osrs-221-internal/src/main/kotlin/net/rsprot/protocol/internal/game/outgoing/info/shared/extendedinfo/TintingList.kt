package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class TintingList : TransientExtendedInfo() {
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
