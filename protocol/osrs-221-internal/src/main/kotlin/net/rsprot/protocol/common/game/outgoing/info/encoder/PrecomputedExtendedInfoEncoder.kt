package net.rsprot.protocol.common.game.outgoing.info.encoder

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.game.outgoing.info.ExtendedInfo

/**
 * Pre-computed extended info encoders encode the data for all necessary extended info blocks
 * early on in the process. This allows us to do a simple native buffer copy to transfer the data over,
 * and avoids us having to re-calculate all the little properties that end up being encoded.
 */
public interface PrecomputedExtendedInfoEncoder<in T : ExtendedInfo<T, *>> : ExtendedInfoEncoder<T> {
    public fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: T,
    ): JagByteBuf
}
