package net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class FaceCoord(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<FaceCoord>>,
) : TransientExtendedInfo<FaceCoord, PrecomputedExtendedInfoEncoder<FaceCoord>>() {
    public var instant: Boolean = false
    public var x: UShort = 0xFFFFu
    public var z: UShort = 0xFFFFu

    override fun clear() {
        releaseBuffers()
        this.instant = false
        this.x = 0xFFFFu
        this.z = 0xFFFFu
    }
}
