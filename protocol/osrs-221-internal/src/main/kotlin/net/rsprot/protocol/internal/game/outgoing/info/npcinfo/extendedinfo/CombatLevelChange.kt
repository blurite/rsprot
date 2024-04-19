package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class CombatLevelChange(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<CombatLevelChange>>,
) : TransientExtendedInfo<CombatLevelChange, PrecomputedExtendedInfoEncoder<CombatLevelChange>>() {
    public var level: Int = -1

    override fun clear() {
        releaseBuffers()
        this.level = -1
    }
}
