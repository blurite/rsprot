package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

/**
 * The extended info block responsible for making an avatar turn towards a specific
 * angle.
 * @param encoders the array of platform-specific encoders for face angle.
 */
public class FaceAngle(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<FaceAngle>>,
) : TransientExtendedInfo<FaceAngle, PrecomputedExtendedInfoEncoder<FaceAngle>>() {
    /**
     * The value of the angle for this avatar to turn towards.
     */
    public var angle: UShort = UShort.MAX_VALUE

    override fun clear() {
        releaseBuffers()
        angle = UShort.MAX_VALUE
    }
}
