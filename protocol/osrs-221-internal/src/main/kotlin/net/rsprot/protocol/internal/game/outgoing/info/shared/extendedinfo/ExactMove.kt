package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

/**
 * The exactmove extended info block is used to provide precise fine-tuned visual movement
 * of an avatar.
 * @param encoders the array of platform-specific encoders for exact move.
 */
public class ExactMove(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<ExactMove>>,
) : TransientExtendedInfo<ExactMove, PrecomputedExtendedInfoEncoder<ExactMove>>() {
    /**
     * The coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     */
    public var deltaX1: UByte = 0u

    /**
     * The coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     */
    public var deltaZ1: UByte = 0u

    /**
     * Delay1 defines how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 1 coordinate.
     */
    public var delay1: UShort = 0u

    /**
     * The coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     */
    public var deltaX2: UByte = 0u

    /**
     * The coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     */
    public var deltaZ2: UByte = 0u

    /**
     * Delay2 defines how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 2 coordinate.
     */
    public var delay2: UShort = 0u

    /**
     * The angle the avatar will be facing throughout the exact movement,
     * with 0 implying south, 512 west, 1024 north and 1536 east; interpolate
     * between to get finer directions.
     */
    public var direction: UShort = 0u

    override fun clear() {
        releaseBuffers()
        deltaX1 = 0u
        deltaZ1 = 0u
        delay1 = 0u
        deltaX2 = 0u
        deltaZ2 = 0u
        delay2 = 0u
        direction = 0u
    }
}
