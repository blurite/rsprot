package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The freeze class is used to temporarily freeze the player or NPC mid-animations.
 */
public class Freeze(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Freeze>>,
) : TransientExtendedInfo<Freeze, PrecomputedExtendedInfoEncoder<Freeze>>() {
    /**
     * The delay in client cycles (20ms/cc) until the freeze is applied.
     * A value of [UShort.MAX_VALUE] resets the existing freeze.
     */
    public var delay: UShort = 0u

    /**
     * The duration in client cycles (20ms/cc) after [delay] until the freeze finishes.
     * A value of [UShort.MAX_VALUE] resets the existing freeze.
     */
    public var duration: UShort = 0u

    /**
     * Whether to cancel the currently-playing sequence. This does not affect
     * base animations.
     */
    public var cancelSequence: Boolean = false

    override fun clear() {
        releaseBuffers()
        delay = 0u
        duration = 0u
        cancelSequence = false
    }
}
