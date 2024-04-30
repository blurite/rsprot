package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.common.client.OldSchoolClientType
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * The info repository class is responsible for allocating and re-using various info implementations.
 */
@Suppress("DuplicatedCode")
internal abstract class InfoRepository<T>(
    private val allocator: (index: Int, oldSchoolClientType: OldSchoolClientType) -> T,
) {
    /**
     * The backing elements array used to store currently-in-use objects.
     */
    protected abstract val elements: Array<T?>

    /**
     * The reference queue used to store soft references of the objects after they have been
     * returned to this structure. The references may release their object if the JVM
     * requires that memory, but only as a last resort, before having to throw an
     * out of memory exception.
     */
    private val queue: ReferenceQueue<T> = ReferenceQueue()

    /**
     * Gets the current element at index [idx], or null if it doesn't exist.
     * @param idx the index of the player info object to obtain
     * @throws ArrayIndexOutOfBoundsException if the index is below zero, or above [capacity].
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    fun getOrNull(idx: Int): T? {
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
    operator fun get(idx: Int): T {
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
        oldSchoolClientType: OldSchoolClientType,
    ): T {
        val element = elements[idx]
        check(element == null) {
            "Overriding existing element: $idx"
        }
        val cached = queue.poll()?.get()
        if (cached != null) {
            onAlloc(cached, idx, oldSchoolClientType)
            elements[idx] = cached
            return cached
        }
        val new = allocator(idx, oldSchoolClientType)
        elements[idx] = new
        return new
    }

    /**
     * The onAlloc function is called when a new element is allocated, necessary to clean up the
     * object before it may be used.
     * @param element the element being allocated
     * @param idx the index of the element
     * @param oldSchoolClientType the client on which the info is being allocated.
     */
    protected abstract fun onAlloc(
        element: T,
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    )

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
            onDealloc(element)
        } finally {
            elements[idx] = null
        }
        informDeallocation(idx)
        val reference = SoftReference(element, queue)
        reference.enqueue()
        return true
    }

    /**
     * Destroys the element at [idx], if there is one.
     * If an object was found, [net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject.onDealloc]
     * function is called on it.
     * This is to clean up any potential memory leaks for objects which may incur such.
     * It should not reset indices and other properties, that should be left to be done
     * during [alloc].
     * Unlike the [dealloc] function, this function will not put the object back into the pool.
     * This is important in case we catch an exception mid-processing, as that will immediately
     * destroy the object, which technically means it could be picked up by another player right
     * away in an unsafe manner. As such, these objects which threw exceptions must be garbage-collected.
     * @param idx the index of the element to deallocate.
     * @throws ArrayIndexOutOfBoundsException if the [idx] is below zero, or above [capacity].
     * @return true if the object was deallocated, false if there was nothing to deallocate.
     */
    fun destroy(idx: Int): Boolean {
        require(idx in elements.indices) {
            "Index out of boundaries: $idx, ${elements.indices}"
        }
        val element =
            elements[idx]
                ?: return false
        try {
            onDealloc(element)
        } finally {
            elements[idx] = null
        }
        informDeallocation(idx)
        return true
    }

    /**
     * The onDealloc function is called when an element is being deallocated.
     * @param element the element being deallocated.
     */
    protected abstract fun onDealloc(element: T)

    /**
     * Informs all the other avatars of a given avatar being deallocated.
     * This is necessary to reset our cached properties (such as the appearance cache)
     * of other players.
     */
    protected abstract fun informDeallocation(idx: Int)
}
