package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.ExtendedInfoFilter
import java.lang.ref.ReferenceQueue

@ExperimentalUnsignedTypes
internal class NpcAvatarRepository(
    private val allocator: ByteBufAllocator,
    private val extendedInfoFilter: ExtendedInfoFilter,
    private val extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    private val huffmanCodec: HuffmanCodec,
) {
    private val elements: Array<NpcAvatar?> = arrayOfNulls(AVATAR_CAPACITY)
    private val queue: ReferenceQueue<NpcAvatar> = ReferenceQueue<NpcAvatar>()

    fun getOrNull(idx: Int): NpcAvatar? {
        return elements[idx]
    }

    fun getOrAlloc(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
    ): NpcAvatar {
        val existing = queue.poll()?.get()
        if (existing != null) {
            // TODO: Reset existing
            elements[index] = existing
            return existing
        }
        val extendedInfo =
            NpcAvatarExtendedInfo(
                index,
                extendedInfoFilter,
                extendedInfoWriter,
                allocator,
                huffmanCodec,
            )
        val avatar =
            NpcAvatar(
                index,
                id,
                level,
                x,
                z,
                spawnCycle,
                direction,
                extendedInfo,
            )
        elements[index] = avatar
        return avatar
    }

    internal companion object {
        internal const val AVATAR_CAPACITY = 65536
    }
}
