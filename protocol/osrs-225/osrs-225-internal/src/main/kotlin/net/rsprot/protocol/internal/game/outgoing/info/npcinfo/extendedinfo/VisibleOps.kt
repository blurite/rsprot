package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class VisibleOps(
	override val encoders: net.rsprot.protocol.internal.client.ClientTypeMap<PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps>>,
) : TransientExtendedInfo<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps, PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps>>() {
    public var ops: UByte =
	    net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps.Companion.DEFAULT_OPS

    override fun clear() {
        releaseBuffers()
        ops = net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps.Companion.DEFAULT_OPS
    }

    public companion object {
        public const val DEFAULT_OPS: UByte = 0b11111u
    }
}
