package net.rsprot.protocol.game.outgoing.info.npcinfo

import java.lang.ref.ReferenceQueue

@ExperimentalUnsignedTypes
internal class NpcAvatarRepository {
    private val elements: Array<NpcAvatar?> = arrayOfNulls(AVATAR_CAPACITY)
    private val queue: ReferenceQueue<NpcAvatar> = ReferenceQueue<NpcAvatar>()

    fun getOrNull(idx: Int): NpcAvatar? {
        return elements[idx]
    }

    internal companion object {
        internal const val AVATAR_CAPACITY = 65536
    }
}
