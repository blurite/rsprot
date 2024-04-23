package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.platform.PlatformMap

public class NameChange(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<NameChange>>,
) : TransientExtendedInfo<NameChange, PrecomputedExtendedInfoEncoder<NameChange>>() {
    public var name: String? = null

    override fun clear() {
        releaseBuffers()
        this.name = null
    }
}