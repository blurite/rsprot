package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class CombatLevelChange(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<CombatLevelChange>>,
) : net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo<CombatLevelChange, PrecomputedExtendedInfoEncoder<CombatLevelChange>>() {
    public var level: Int = DEFAULT_LEVEL_OVERRIDE

    override fun clear() {
        releaseBuffers()
        this.level = DEFAULT_LEVEL_OVERRIDE
    }

    public companion object {
        public const val DEFAULT_LEVEL_OVERRIDE: Int = -1
    }
}
