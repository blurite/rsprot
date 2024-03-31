package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public abstract class AvatarExtendedInfoWriter(
    public val platformType: PlatformType,
    public val encoders: ExtendedInfoEncoders,
) {
    public abstract fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: PlayerAvatarExtendedInfoBlocks,
    )

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
