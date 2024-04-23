package net.rsprot.protocol.game.outgoing.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.common.platform.PlatformType

/**
 * A base class for platform-specific extended info writers.
 * @param platformType the platform for which the encoders are created.
 * @param encoders the set of extended info encoders for the given [platformType].
 */
public abstract class AvatarExtendedInfoWriter<E, B>(
    public val platformType: PlatformType,
    public val encoders: E,
) {
    /**
     * Main function to write all the extended info blocks over.
     * The extended info blocks must be in the exact order as they are
     * read within the client, and this function is responsible
     * for converting library-specific-constants to platform-specific-flags.
     *
     * @param buffer the buffer into which to write the extended info block.
     * @param localIndex the index of the avatar that owns these extended info blocks.
     * @param observerIndex the index of the player observing this avatar.
     * @param flag the constant-flag of all the extended info blocks which must be
     * translated and written to the buffer.
     * @param blocks the wrapper class around all the extended info blocks.
     * The blocks which are flagged will be written over.
     */
    public abstract fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: B,
    )

    /**
     * Natively copies cached data from the pre-computed extended info buffer over
     * into the primary player info buffer.
     * @param buffer the primary player info buffer.
     * @param block the extended info block which to copy over.
     * @throws IllegalStateException if the given buffer has not been precomputed
     * for the given platform type.
     */
    protected fun pCachedData(
        buffer: JagByteBuf,
        block: ExtendedInfo<*, *>,
    ) {
        val precomputed =
            checkNotNull(block.getBuffer(platformType)) {
                "Buffer has not been computed on platform $platformType, ${block.javaClass.name}"
            }
        buffer.buffer.writeBytes(precomputed, precomputed.readerIndex(), precomputed.readableBytes())
    }

    /**
     * Writes on-demand extended info block. This is for extended info blocks which
     * cannot be pre-computed as they depend on the observer for information,
     * such as tinted hitmarks.
     * @param buffer the primary player info buffer.
     * @param localIndex the index of the avatar that owns this extended info block.
     * @param block the extended info block to compute and write into the primary buffer.
     * @param observerIndex the index of the avatar observing the avatar who owns this
     * extended info block.
     */
    protected fun <T : ExtendedInfo<T, E>, E : OnDemandExtendedInfoEncoder<T>> pOnDemandData(
        buffer: JagByteBuf,
        localIndex: Int,
        block: T,
        observerIndex: Int,
    ) {
        val encoder =
            checkNotNull(block.getEncoder(platformType)) {
                "Encoder has not been set for platform $platformType"
            }
        encoder.encode(
            buffer,
            observerIndex,
            localIndex,
            block,
        )
    }
}
