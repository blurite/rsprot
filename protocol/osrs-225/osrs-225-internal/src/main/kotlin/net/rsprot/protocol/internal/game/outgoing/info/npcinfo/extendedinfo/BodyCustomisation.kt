package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class BodyCustomisation(
	override val encoders: net.rsprot.protocol.internal.client.ClientTypeMap<PrecomputedExtendedInfoEncoder<BodyCustomisation>>,
) : TransientExtendedInfo<BodyCustomisation, PrecomputedExtendedInfoEncoder<BodyCustomisation>>() {
    public var customisation: net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.TypeCustomisation? = null

    override fun clear() {
        releaseBuffers()
        this.customisation = null
    }
}
