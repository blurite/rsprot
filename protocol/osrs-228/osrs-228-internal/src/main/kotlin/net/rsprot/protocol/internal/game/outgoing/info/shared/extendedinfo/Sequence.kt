package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The sequence mask defines what animation an avatar is playing.
 * @param encoders the array of client-specific encoders for sequence.
 */
public class Sequence(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Sequence>>,
) : TransientExtendedInfo<Sequence, PrecomputedExtendedInfoEncoder<Sequence>>() {
    /**
     * The id of the animation to play.
     */
    public var id: UShort = 0xFFFFu

    /**
     * The delay in client cycles (20ms/cc) until the given animation begins playing.
     */
    public var delay: UShort = 0u

    override fun clear() {
        releaseBuffers()
        id = 0xFFFFu
        delay = 0u
    }
}
