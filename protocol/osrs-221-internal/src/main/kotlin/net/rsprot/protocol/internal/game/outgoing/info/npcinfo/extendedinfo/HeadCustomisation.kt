package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class HeadCustomisation(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<HeadCustomisation>>,
) : TransientExtendedInfo<HeadCustomisation, PrecomputedExtendedInfoEncoder<HeadCustomisation>>() {
    public var customisation: TypeCustomisation? = null

    override fun clear() {
        releaseBuffers()
        this.customisation = null
    }
}
