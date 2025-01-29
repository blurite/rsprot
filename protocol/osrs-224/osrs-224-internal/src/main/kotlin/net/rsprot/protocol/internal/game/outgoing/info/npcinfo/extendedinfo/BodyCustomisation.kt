package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class BodyCustomisation(
	override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation>>,
) : TransientExtendedInfo<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation, PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation>>() {
    public var customisation: net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.TypeCustomisation? = null

    override fun clear() {
        releaseBuffers()
        this.customisation = null
    }
}
