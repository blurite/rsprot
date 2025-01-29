package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting

/**
 * The tinting extended info block.
 * This is a rather special case as tinting is one of the two observer-dependent extended info blocks,
 * along with [net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit].
 * It is possible for the server to mark tinting for only a single avatar to see.
 * In order to achieve this, we utilize [observerDependent] tinting, indexed by the observer's id.
 * @param encoders the array of client-specific encoders for tinting.
 */
public class PlayerTintingList(
    override val encoders: ClientTypeMap<OnDemandExtendedInfoEncoder<PlayerTintingList>>,
) : TransientExtendedInfo<PlayerTintingList, OnDemandExtendedInfoEncoder<PlayerTintingList>>() {
    public val global: net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting =
	    net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting()
    public val observerDependent: MutableMap<Int, net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting> = HashMap()

    public operator fun get(index: Int): net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting = observerDependent.getOrDefault(index, global)

    override fun clear() {
        releaseBuffers()
        global.reset()
        if (observerDependent.isNotEmpty()) {
            observerDependent.clear()
        }
    }
}
