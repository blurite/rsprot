package net.rsprot.protocol.message.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.message.ZoneProt

public interface UpdateZonePartialEnclosedCache {
    public fun <T : ZoneProt> buildCache(
        allocator: ByteBufAllocator,
        messages: Collection<T>,
    ): ByteBuf
}
