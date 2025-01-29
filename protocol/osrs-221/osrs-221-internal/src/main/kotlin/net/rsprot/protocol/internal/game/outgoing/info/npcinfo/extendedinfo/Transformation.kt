package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class Transformation(
	override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.Transformation>>,
) : net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.Transformation, PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.Transformation>>() {
    public var id: UShort = 0xFFFFu

    override fun clear() {
        releaseBuffers()
        this.id = 0xFFFFu
    }
}
