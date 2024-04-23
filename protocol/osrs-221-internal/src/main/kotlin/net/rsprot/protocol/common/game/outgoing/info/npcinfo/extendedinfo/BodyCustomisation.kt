package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.platform.PlatformMap

public class BodyCustomisation(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<BodyCustomisation>>,
) : TransientExtendedInfo<BodyCustomisation, PrecomputedExtendedInfoEncoder<BodyCustomisation>>() {
    public var customisation: TypeCustomisation? = null

    override fun clear() {
        releaseBuffers()
        this.customisation = null
    }
}
