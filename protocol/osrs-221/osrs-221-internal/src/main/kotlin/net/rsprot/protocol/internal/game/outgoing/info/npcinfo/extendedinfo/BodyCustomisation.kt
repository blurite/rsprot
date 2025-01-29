package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class BodyCustomisation(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<BodyCustomisation>>,
) : net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo<BodyCustomisation, PrecomputedExtendedInfoEncoder<BodyCustomisation>>() {
    public var customisation: TypeCustomisation? = null

    override fun clear() {
        releaseBuffers()
        this.customisation = null
    }
}
