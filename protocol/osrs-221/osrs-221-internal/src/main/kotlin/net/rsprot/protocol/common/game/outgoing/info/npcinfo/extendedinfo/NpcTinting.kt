package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.Tinting

/**
 * The tinting extended info block.
 * @param encoders the array of client-specific encoders for tinting.
 */
public class NpcTinting(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<NpcTinting>>,
) : TransientExtendedInfo<NpcTinting, PrecomputedExtendedInfoEncoder<NpcTinting>>() {
    public val global: Tinting = Tinting()

    override fun clear() {
        releaseBuffers()
        global.reset()
    }
}
