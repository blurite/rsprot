package net.rsprot.protocol.internal.game.outgoing.info.encoder

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo

/**
 * On-demand extended info encoders are invoked on every observer whenever information must be written.
 * These differ from [PrecomputedExtendedInfoEncoder] in that they cannot be pre-computed, as the
 * data in the buffer is dependent on the observer.
 */
public interface OnDemandExtendedInfoEncoder<in T : ExtendedInfo<T, *>> : ExtendedInfoEncoder<T> {
    public fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedAvatarIndex: Int,
        extendedInfo: T,
    )
}
