package net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The extended info block responsible for making an avatar turn towards a specific
 * angle.
 * @param encoders the array of client-specific encoders for face angle.
 */
public class FaceAngle(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<FaceAngle>>,
) : TransientExtendedInfo<FaceAngle, PrecomputedExtendedInfoEncoder<FaceAngle>>() {
    /**
     * The value of the angle for this avatar to turn towards.
     */
    public var angle: UShort = UShort.MAX_VALUE
    public var outOfDate: Boolean = false
        private set

    public fun markUpToDate() {
        if (!outOfDate) {
            return
        }
        outOfDate = false
        releaseBuffers()
    }

    public fun syncAngle(angle: Int) {
        this.outOfDate = true
        this.angle = angle.toUShort()
    }

    override fun clear() {
        releaseBuffers()
        angle = UShort.MAX_VALUE
        outOfDate = false
    }

    public companion object {
        public val DEFAULT_VALUE: UShort = UShort.MAX_VALUE
    }
}
