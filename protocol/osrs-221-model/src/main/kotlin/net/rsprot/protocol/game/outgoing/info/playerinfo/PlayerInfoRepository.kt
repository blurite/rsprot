package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.shared.platform.PlatformType
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * An array implementation that utilizes a reference queue to re-use objects created in the past.
 * This is particularly useful when dealing with objects that consume a lot of memory,
 * pooling in such cases prevents time taken by allocations and garbage collection.
 *
 * This particular implementation uses soft references to keep track of the deallocated
 * objects. Soft references only release their object if the JVM is about to run out of
 * memory, as a last resort.
 *
 * @param allocator the function that yields new elements on-demand, if none
 * are available within the reference queue.
 */
@Suppress("unused")
internal class PlayerInfoRepository(
    private val allocator: (index: Int, platformType: PlatformType) -> PlayerInfo,
) {
    /**
     * The backing elements array used to store currently-in-use objects.
     */
    private val elements: Array<PlayerInfo?> = arrayOfNulls(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    /**
     * The reference queue used to store soft references of the objects after they have been
     * returned to this structure. The references may release their object if the JVM
     * requires that memory, but only as a last resort, before having to throw an
     * out of memory exception.
     */
    private val queue: ReferenceQueue<PlayerInfo> = ReferenceQueue()

    /**
     * Gets the current element at index [idx], or null if it doesn't exist.
     * @param idx the index of the player info object to obtain
     * @throws ArrayIndexOutOfBoundsException if the index is below zero, or above [capacity].
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    fun getOrNull(idx: Int): PlayerInfo? {
        return elements[idx]
    }

    /**
     * Gets the current element at index [idx].
     * @param idx the index of the player info object to obtain
     * @throws ArrayIndexOutOfBoundsException if the index is below zero, or above [capacity].
     * @throws IllegalStateException if the element at index [idx] is null.
     */
    @Throws(
        ArrayIndexOutOfBoundsException::class,
        IllegalStateException::class,
    )
    operator fun get(idx: Int): PlayerInfo {
        return checkNotNull(elements[idx])
    }

    /**
     * Gets the maximum capacity of this object array.
     */
    fun capacity(): Int {
        return elements.size
    }

    /**
     * Allocates a new element at the specified [idx].
     * This function will first check if there are any unused objects
     * left in the [queue]. If there are, obtains the reference and executes
     * [net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject.onAlloc] in it,
     * which is responsible for cleaning the object so that it can be re-used again.
     * This is preferably done on allocations, rather than de-allocations,
     * as there's a chance the JVM will just garbage collect
     * the object without it ever being re-allocated.
     *
     * @param idx the index of the element to obtain.
     * @throws ArrayIndexOutOfBoundsException if the [idx] is below zero, or above [capacity].
     * @throws IllegalStateException if the element at index [idx] is already in use.
     */
    @Throws(
        ArrayIndexOutOfBoundsException::class,
        IllegalStateException::class,
    )
    fun alloc(
        idx: Int,
        platformType: PlatformType,
    ): PlayerInfo {
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

    /**
     * Deallocates the element at [idx], if there is one.
     * If an object was found, [net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject.onDealloc]
     * function is called on it.
     * This is to clean up any potential memory leaks for objects which may incur such.
     * It should not reset indices and other properties, that should be left to be done
     * during [alloc].
     * @param idx the index of the element to deallocate.
     * @throws ArrayIndexOutOfBoundsException if the [idx] is below zero, or above [capacity].
     * @return true if the object was deallocated, false if there was nothing to deallocate.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
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

    /**
     * Informs all the other avatars of a given avatar being deallocated.
     * This is necessary to reset our cached properties (such as the appearance cache)
     * of other players.
     */
    private fun informDeallocation(idx: Int) {
        for (element in elements) {
            if (element == null) {
                continue
            }
            element.avatar.extendedInfo.onOtherAvatarDeallocated(idx)
        }
    }
}
