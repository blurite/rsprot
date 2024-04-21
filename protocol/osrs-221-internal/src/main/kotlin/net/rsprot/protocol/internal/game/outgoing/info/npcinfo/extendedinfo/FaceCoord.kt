package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class FaceCoord(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<FaceCoord>>,
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
