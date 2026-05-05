package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

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
    public var instant: Boolean = DEFAULT_INSTANT

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
        instant = DEFAULT_INSTANT
        outOfDate = false
    }

    public companion object {
        public val DEFAULT_VALUE: UShort = UShort.MAX_VALUE
        public const val DEFAULT_INSTANT: Boolean = false
    }
}
