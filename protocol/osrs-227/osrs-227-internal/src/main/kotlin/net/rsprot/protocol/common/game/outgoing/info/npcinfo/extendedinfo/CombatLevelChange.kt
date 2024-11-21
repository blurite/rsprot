package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class CombatLevelChange(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<CombatLevelChange>>,
) : TransientExtendedInfo<CombatLevelChange, PrecomputedExtendedInfoEncoder<CombatLevelChange>>() {
    public var level: Int = DEFAULT_LEVEL_OVERRIDE

    override fun clear() {
        releaseBuffers()
        this.level = DEFAULT_LEVEL_OVERRIDE
    }

    public companion object {
        public const val DEFAULT_LEVEL_OVERRIDE: Int = -1
    }
}
