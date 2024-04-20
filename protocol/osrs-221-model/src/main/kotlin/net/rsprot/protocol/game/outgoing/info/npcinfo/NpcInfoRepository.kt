package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.protocol.shared.platform.PlatformType
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

// TODO: Bring it together with player info?
@ExperimentalUnsignedTypes
internal class NpcInfoRepository(
    private val allocator: (index: Int, platformType: PlatformType) -> NpcInfo,
) {
    private val elements: Array<NpcInfo?> = arrayOfNulls(NpcInfoProtocol.PROTOCOL_CAPACITY)
    private val queue: ReferenceQueue<NpcInfo> = ReferenceQueue()

    @Throws(ArrayIndexOutOfBoundsException::class)
    fun getOrNull(idx: Int): NpcInfo? {
        return elements[idx]
    }

    operator fun get(idx: Int): NpcInfo {
        return checkNotNull(elements[idx])
    }

    fun capacity(): Int {
        return elements.size
    }

    fun alloc(
        idx: Int,
        platformType: PlatformType,
    ): NpcInfo {
        val element = elements[idx]
        check(element == null) {
            "Overriding existing element: $idx"
        }
        val cached = queue.poll()?.get()
        if (cached != null) {
            cached.onAlloc(idx, platformType)
            elements[idx] = cached
            return cached
        }
        val new = allocator(idx, platformType)
        elements[idx] = new
        return new
    }

    fun dealloc(idx: Int): Boolean {
        require(idx in elements.indices) {
            "Index out of boundaries: $idx, ${elements.indices}"
        }
        val element =
            elements[idx]
                ?: return false
        try {
            element.onDealloc()
        } finally {
            elements[idx] = null
        }
        informDeallocation(idx)
        val reference = SoftReference(element, queue)
        reference.enqueue()
        return true
    }

    fun informDeallocation(idx: Int) {
        // TODO
    }
}
