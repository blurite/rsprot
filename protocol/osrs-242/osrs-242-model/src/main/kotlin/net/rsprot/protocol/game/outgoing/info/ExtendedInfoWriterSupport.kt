package net.rsprot.protocol.game.outgoing.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder

/**
 * Shared support for client-specific extended info writers.
 * @param oldSchoolClientType the client for which the encoders are created.
 * @param encoders the set of extended info encoders for the given [oldSchoolClientType].
 */
public abstract class ExtendedInfoWriterSupport<E>(
    public val oldSchoolClientType: OldSchoolClientType,
    public val encoders: E,
) {
    /**
     * Natively copies cached data from the pre-computed extended info buffer over
     * into the primary player info buffer.
     * @param buffer the primary player info buffer.
     * @param block the extended info block which to copy over.
     * @throws IllegalStateException if the given buffer has not been precomputed
     * for the given client type.
     */
    protected fun pCachedData(
        buffer: JagByteBuf,
        block: ExtendedInfo<*, *>,
    ) {
        val precomputed =
            checkNotNull(block.getBuffer(oldSchoolClientType)) {
                "Buffer has not been computed on client $oldSchoolClientType, ${block.javaClass.name}"
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
            checkNotNull(block.getEncoder(oldSchoolClientType)) {
                "Encoder has not been set for client $oldSchoolClientType"
            }
        encoder.encode(
            buffer,
            observerIndex,
            localIndex,
            block,
        )
    }
}
