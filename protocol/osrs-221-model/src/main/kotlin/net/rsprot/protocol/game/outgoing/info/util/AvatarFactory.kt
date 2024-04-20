package net.rsprot.protocol.game.outgoing.info.util

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.ExtendedInfoFilter

public abstract class AvatarFactory<WriterType>(
    protected val allocator: ByteBufAllocator,
    protected val extendedInfoFilter: ExtendedInfoFilter,
    protected val extendedInfoWriter: List<WriterType>,
    protected val huffmanCodec: HuffmanCodec,
) {
    public abstract fun alloc(index: Int): Avatar
}
