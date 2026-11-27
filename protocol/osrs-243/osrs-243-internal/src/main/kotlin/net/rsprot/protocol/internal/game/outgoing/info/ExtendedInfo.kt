package net.rsprot.protocol.internal.game.outgoing.info

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ReferenceCountUtil
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The abstract extended info class, responsible for holding some
 * information about a specific avatar.
 * @param T the extended info block type.
 * @param E the encoder for the given extended info block [T].
 */
public abstract class ExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>> {
    public abstract val encoders: ClientTypeMap<E>

    /**
     * An array of client-specific pre-computed buffers of this extended info block.
     * These buffers get pre-computed during player info building process,
     * and the pre-computed buffers will be natively copied over to the main buffer
     * by all the observers.
     * For extended info blocks which cannot be pre-computed, the building happens on-demand.
     */
    private val buffers: Array<ByteBuf?> = arrayOfNulls(OldSchoolClientType.COUNT)

    /**
     * Sets the client-specific [buffer] at index [clientTypeId].
     * @param clientTypeId the id of the client, additionally used as the key to the [buffers] array.
     * @param buffer the pre-computed buffer for this extended info block.
     */
    public fun setBuffer(
        clientTypeId: Int,
        buffer: ByteBuf,
    ) {
        buffers[clientTypeId] = buffer
    }

    /**
     * Gets the latest pre-computed buffer for the given [oldSchoolClientType].
     * @param oldSchoolClientType the client for which to obtain the buffer.
     * @return the pre-computed buffer, or null if it does not exist.
     */
    public fun getBuffer(oldSchoolClientType: OldSchoolClientType): ByteBuf? = buffers[oldSchoolClientType.id]

    /**
     * Gets the encoder for a given [oldSchoolClientType].
     * @param oldSchoolClientType the client type for which to obtain the encoder.
     * @return the client-specific encoder of this extended info block, or null
     * if one has not been registered.
     */
    public fun getEncoder(oldSchoolClientType: OldSchoolClientType): E? = encoders.getOrNull(oldSchoolClientType)

    /**
     * Releases all the client-specific buffers of this extended info block,
     * which will either be garbage-collected or returned into the bytebuf pool.
     */
    internal fun releaseBuffers() {
        try {
            for (i in 0..<OldSchoolClientType.COUNT) {
                val buffer = buffers[i] ?: continue
                val refCnt = buffer.refCnt()
                if (refCnt > 0) {
                    ReferenceCountUtil.safeRelease(buffer, refCnt)
                }
                buffers[i] = null
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to release old buffers"
            }
        }
    }

    /**
     * Checks whether a buffer has been precomputed on the specified client type.
     * @param oldSchoolClientType the client for which to check a precomputed buffer.
     * @return whether the buffer has been precomputed for the specified client.
     */
    public fun isPrecomputed(oldSchoolClientType: OldSchoolClientType): Boolean =
        buffers[oldSchoolClientType.id] != null

    /**
     * Clears this extended info block, making it ready for use by another avatar.
     */
    public abstract fun clear()

    private companion object {
        private val logger = InlineLogger()
    }
}

/**
 * A function to pre-compute this extended info block.
 * Extended info blocks which do not support pre-computing (meaning they are observer-dependent)
 * will build the buffer on-demand per observer.
 */
public fun <T : ExtendedInfo<T, E>, E : PrecomputedExtendedInfoEncoder<T>> T.precompute(
    allocator: ByteBufAllocator,
    huffmanCodecProvider: HuffmanCodecProvider,
) {
    // Release any old buffers before overwriting with new ones
    releaseBuffers()
    for (id in 0..<OldSchoolClientType.COUNT) {
        val encoder = encoders.getOrNull(id) ?: continue
        val encoded =
            encoder.precompute(
                allocator,
                huffmanCodecProvider,
                this,
            )
        setBuffer(id, encoded.buffer)
    }
}
