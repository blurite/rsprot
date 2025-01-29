package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class CombatLevelChange(
	override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange>>,
) : TransientExtendedInfo<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange, PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange>>() {
    public var level: Int =
	    net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange.Companion.DEFAULT_LEVEL_OVERRIDE

    override fun clear() {
        releaseBuffers()
        this.level =
	        net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange.Companion.DEFAULT_LEVEL_OVERRIDE
    }

    public companion object {
        public const val DEFAULT_LEVEL_OVERRIDE: Int = -1
    }
}
