package net.rsprot.protocol.internal.game.outgoing.info

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public abstract class ExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>>(
    protected val encoders: Array<E?>,
) {
    private val buffers: Array<ByteBuf?> = arrayOfNulls(PlatformType.COUNT)

    public abstract fun precompute()

    protected fun setBuffer(
        platformTypeId: Int,
        buffer: ByteBuf,
    ) {
        buffers[platformTypeId] = buffer
    }

    public fun getBuffer(platformType: PlatformType): ByteBuf? {
        return buffers[platformType.id]
    }

    public fun getEncoder(platformType: PlatformType): E? {
        return encoders[platformType.id]
    }

    protected fun releaseBuffers() {
        for (i in 0..<PlatformType.COUNT) {
            val buffer = buffers[i] ?: continue
            buffer.release()
            buffers[i] = null
        }
    }

    public abstract fun clear()
}
