package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter

public class PlayerAvatarFactory(
    private val allocator: ByteBufAllocator,
    private val extendedInfoFilter: ExtendedInfoFilter,
    private val extendedInfoWriter: List<PlayerAvatarExtendedInfoWriter>,
    private val huffmanCodec: HuffmanCodec,
) {
    public fun alloc(index: Int): PlayerAvatar {
        // It is possible to just pass in the extended info from here, but based on benchmarks,
        // due to the field order changing, the performance will absolutely tank in doing so,
        // going from ~160ms in the benchmark to around 200ms
        return PlayerAvatar(
            allocator,
            index,
            extendedInfoFilter,
            extendedInfoWriter,
            huffmanCodec,
        )
    }
}
