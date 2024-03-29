package net.rsprot.protocol.game.outgoing.info.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo

/**
 * Pre-computed extended info encoders encode the data for all necessary extended info blocks
 * early on in the process. This allows us to do a simple native buffer copy to transfer the data over,
 * and avoids us having to re-calculate all the little properties that end up being encoded.
 */
public interface PrecomputedExtendedInfoEncoder<in T : ExtendedInfo> : ExtendedInfoEncoder<T> {
    public fun precompute(
        alloc: ByteBufAllocator,
        extendedInfo: T,
    ): JagByteBuf
}
