package net.rsprot.protocol.internal.game.outgoing.info

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The abstract extended info class, responsible for holding some
 * information about a specific avatar.
 * @param T the extended info block type.
 * @param E the encoder for the given extended info block [T].
 */
public abstract class ExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>> {
    public abstract val encoders: PlatformMap<E>

    /**
     * An array of platform-specific pre-computed buffers of this extended info block.
     * These buffers get pre-computed during player info building process,
     * and the pre-computed buffers will be natively copied over to the main buffer
     * by all the observers.
     * For extended info blocks which cannot be pre-computed, the building happens on-demand.
     */
    private val buffers: Array<ByteBuf?> = arrayOfNulls(PlatformType.COUNT)

    /**
     * Sets the platform-specific [buffer] at index [platformTypeId].
     * @param platformTypeId the id of the platform, additionally used as the key to the [buffers] array.
     * @param buffer the pre-computed buffer for this extended info block.
     */
    public fun setBuffer(
        platformTypeId: Int,
        buffer: ByteBuf,
    ) {
        buffers[platformTypeId] = buffer
    }

    /**
     * Gets the latest pre-computed buffer for the given [platformType].
     * @param platformType the platform for which to obtain the buffer.
     * @return the pre-computed buffer, or null if it does not exist.
     */
    public fun getBuffer(platformType: PlatformType): ByteBuf? {
        return buffers[platformType.id]
    }

    /**
     * Gets the encoder for a given [platformType].
     * @param platformType the platform type for which to obtain the encoder.
     * @return the platform-specific encoder of this extended info block, or null
     * if one has not been registered.
     */
    public fun getEncoder(platformType: PlatformType): E? {
        return encoders.getOrNull(platformType)
    }

    /**
     * Releases all the platform-specific buffers of this extended info block,
     * which will either be garbage-collected or returned into the bytebuf pool.
     */
    protected fun releaseBuffers() {
        for (i in 0..<PlatformType.COUNT) {
            val buffer = buffers[i] ?: continue
            buffer.release()
            buffers[i] = null
        }
    }

    /**
     * Clears this extended info block, making it ready for use by another avatar.
     */
    public abstract fun clear()
}

/**
 * A function to pre-compute this extended info block.
 * Extended info blocks which do not support pre-computing (meaning they are observer-dependent)
 * will build the buffer on-demand per observer.
 */
public fun <T : ExtendedInfo<T, E>, E : PrecomputedExtendedInfoEncoder<T>> T.precompute(
    allocator: ByteBufAllocator,
    huffmanCodec: HuffmanCodec,
) {
    for (id in 0..<PlatformType.COUNT) {
        val encoder = encoders.getOrNull(id) ?: continue
        val encoded =
            encoder.precompute(
                allocator,
                huffmanCodec,
                this,
            )
        setBuffer(id, encoded.buffer)
    }
}
