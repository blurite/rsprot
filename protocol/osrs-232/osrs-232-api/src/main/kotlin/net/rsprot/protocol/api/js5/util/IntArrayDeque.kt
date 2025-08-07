package net.rsprot.protocol.api.js5.util

/**
 * A specialized array deque for int types, allowing no-autoboxing implementation.
 * This is effectively just the Kotlin stdlib ArrayDeque class copied over and adjusted
 * to work off of the primitive int.
 */
@Suppress("ReplaceUntilWithRangeUntil", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")
public class IntArrayDeque {
    private var head: Int = 0
    private var elementData: IntArray

    public var size: Int = 0
        private set

    public val lastIndex: Int
        get() = elementData.size - 1

    /**
     * Constructs an empty deque with specified [initialCapacity], or throws [IllegalArgumentException] if [initialCapacity] is negative.
     */
    public constructor(
        initialCapacity: Int,
    ) {
        elementData =
            when {
                initialCapacity == 0 -> emptyElementData
                initialCapacity > 0 -> IntArray(initialCapacity)
                else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
            }
    }

    /**
     * Constructs an empty deque.
     */
    public constructor() {
        elementData = emptyElementData
    }

    /**
     * Constructs a deque that contains the same elements as the specified [elements] collection in the same order.
     */
    public constructor(
        elements: Collection<Int>,
    ) {
        elementData = elements.toIntArray()
        size = elementData.size
        if (elementData.isEmpty()) elementData = emptyElementData
    }

    /**
     * Ensures that the capacity of this deque is at least equal to the specified [minCapacity].
     *
     * If the current capacity is less than the [minCapacity], a new backing storage is allocated with greater capacity.
     * Otherwise, this method takes no action and simply returns.
     */
    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity < 0) throw IllegalStateException("Deque is too big.") // overflow
        if (minCapacity <= elementData.size) return
        if (elementData === emptyElementData) {
            elementData = IntArray(minCapacity.coerceAtLeast(DEFAULT_MIN_CAPACITY))
            return
        }

        val newCapacity = newCapacity(elementData.size, minCapacity)
        copyElements(newCapacity)
    }

    /**
     * Creates a new array with the specified [newCapacity] size and copies elements in the [elementData] array to it.
     */
    private fun copyElements(newCapacity: Int) {
        val newElements = IntArray(newCapacity)
        elementData.copyInto(newElements, 0, head, elementData.size)
        elementData.copyInto(newElements, elementData.size - head, 0, head)
        head = 0
        elementData = newElements
    }

    private inline fun internalGet(internalIndex: Int): Int = elementData[internalIndex]

    private fun positiveMod(index: Int): Int = if (index >= elementData.size) index - elementData.size else index

    private fun negativeMod(index: Int): Int = if (index < 0) index + elementData.size else index

    private inline fun internalIndex(index: Int): Int = positiveMod(head + index)

    private fun incremented(index: Int): Int = if (index == elementData.lastIndex) 0 else index + 1

    private fun decremented(index: Int): Int = if (index == 0) elementData.lastIndex else index - 1

    public fun isEmpty(): Boolean = size == 0

    public inline fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * Returns the first element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun first(): Int = if (isEmpty()) throw NoSuchElementException("ArrayDeque is empty.") else internalGet(head)

    /**
     * Returns the first element, or `null` if this deque is empty.
     */
    public fun firstOrNull(): Int? = if (isEmpty()) null else internalGet(head)

    /**
     * Returns the last element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun last(): Int =
        if (isEmpty()) throw NoSuchElementException("ArrayDeque is empty.") else internalGet(internalIndex(lastIndex))

    /**
     * Returns the last element, or `null` if this deque is empty.
     */
    public fun lastOrNull(): Int? = if (isEmpty()) null else internalGet(internalIndex(lastIndex))

    /**
     * Prepends the specified [element] to this deque.
     */
    public fun addFirst(element: Int) {
        ensureCapacity(size + 1)

        head = decremented(head)
        elementData[head] = element
        size += 1
    }

    /**
     * Appends the specified [element] to this deque.
     */
    public fun addLast(element: Int) {
        ensureCapacity(size + 1)

        elementData[internalIndex(size)] = element
        size += 1
    }

    /**
     * Removes the first element from this deque and returns that removed element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun removeFirst(): Int {
        if (isEmpty()) throw NoSuchElementException("ArrayDeque is empty.")

        val element = internalGet(head)
        elementData[head] = 0
        head = incremented(head)
        size -= 1
        return element
    }

    /**
     * Removes the first element from this deque and returns that removed element, or returns `null` if this deque is empty.
     */
    public fun removeFirstOrNull(): Int? = if (isEmpty()) null else removeFirst()

    /**
     * Removes the first element from this deque and returns that removed element,
     * or returns [default] if this deque is empty.
     */
    public fun removeFirstOrDefault(default: Int): Int = if (isEmpty()) default else removeFirst()

    /**
     * Removes the last element from this deque and returns that removed element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun removeLast(): Int {
        if (isEmpty()) throw NoSuchElementException("ArrayDeque is empty.")

        val internalLastIndex = internalIndex(lastIndex)
        val element = internalGet(internalLastIndex)
        elementData[internalLastIndex] = 0
        size -= 1
        return element
    }

    /**
     * Removes the last element from this deque and returns that removed element, or returns `null` if this deque is empty.
     */
    public fun removeLastOrNull(): Int? = if (isEmpty()) null else removeLast()

    // MutableList, MutableCollection
    public fun add(element: Int): Boolean {
        addLast(element)
        return true
    }

    public fun add(
        index: Int,
        element: Int,
    ) {
        checkPositionIndex(index, size)

        if (index == size) {
            addLast(element)
            return
        } else if (index == 0) {
            addFirst(element)
            return
        }

        ensureCapacity(size + 1)

        // Elements in circular array lay in 2 ways:
        //   1. `head` is less than `tail`:       [#, #, e1, e2, e3, #]
        //   2. `head` is greater than `tail`:    [e3, #, #, #, e1, e2]
        // where head is the index of the first element in the circular array,
        // and tail is the index following the last element.
        //
        // At this point the insertion index is not equal to head or tail.
        // Also the circular array can store at least one more element.
        //
        // Depending on where the given element must be inserted the preceding or the succeeding
        // elements will be shifted to make room for the element to be inserted.
        //
        // In case the preceding elements are shifted:
        //   * if the insertion index is greater than the head (regardless of circular array form)
        //      -> shift the preceding elements
        //   * otherwise, the circular array has (2) form and the insertion index is less than tail
        //      -> shift all elements in the back of the array
        //      -> shift preceding elements in the front of the array
        // In case the succeeding elements are shifted:
        //   * if the insertion index is less than the tail (regardless of circular array form)
        //      -> shift the succeeding elements
        //   * otherwise, the circular array has (2) form and the insertion index is greater than head
        //      -> shift all elements in the front of the array
        //      -> shift succeeding elements in the back of the array

        val internalIndex = internalIndex(index)

        if (index < (size + 1) shr 1) {
            // closer to the first element -> shift preceding elements
            val decrementedInternalIndex = decremented(internalIndex)
            val decrementedHead = decremented(head)

            if (decrementedInternalIndex >= head) {
                elementData[decrementedHead] = elementData[head] // head can be zero
                elementData.copyInto(elementData, head, head + 1, decrementedInternalIndex + 1)
            } else { // head > tail
                elementData.copyInto(elementData, head - 1, head, elementData.size) // head can't be zero
                elementData[elementData.size - 1] = elementData[0]
                elementData.copyInto(elementData, 0, 1, decrementedInternalIndex + 1)
            }

            elementData[decrementedInternalIndex] = element
            head = decrementedHead
        } else {
            // closer to the last element -> shift succeeding elements
            val tail = internalIndex(size)

            if (internalIndex < tail) {
                elementData.copyInto(elementData, internalIndex + 1, internalIndex, tail)
            } else { // head > tail
                elementData.copyInto(elementData, 1, 0, tail)
                elementData[0] = elementData[elementData.size - 1]
                elementData.copyInto(elementData, internalIndex + 1, internalIndex, elementData.size - 1)
            }

            elementData[internalIndex] = element
        }
        size += 1
    }

    private fun copyCollectionElements(
        internalIndex: Int,
        elements: Collection<Int>,
    ) {
        val iterator = elements.iterator()

        for (index in internalIndex until elementData.size) {
            if (!iterator.hasNext()) break
            elementData[index] = iterator.next()
        }
        for (index in 0 until head) {
            if (!iterator.hasNext()) break
            elementData[index] = iterator.next()
        }

        size += elements.size
    }

    public fun addAll(elements: Collection<Int>): Boolean {
        if (elements.isEmpty()) return false
        ensureCapacity(this.size + elements.size)
        copyCollectionElements(internalIndex(size), elements)
        return true
    }

    public fun addAll(
        index: Int,
        elements: Collection<Int>,
    ): Boolean {
        checkPositionIndex(index, size)

        if (elements.isEmpty()) {
            return false
        } else if (index == size) {
            return addAll(elements)
        }

        ensureCapacity(this.size + elements.size)

        val tail = internalIndex(size)
        val internalIndex = internalIndex(index)
        val elementsSize = elements.size

        if (index < (size + 1) shr 1) {
            // closer to the first element -> shift preceding elements

            var shiftedHead = head - elementsSize

            if (internalIndex >= head) {
                if (shiftedHead >= 0) {
                    elementData.copyInto(elementData, shiftedHead, head, internalIndex)
                } else { // head < tail, insertion leads to head >= tail
                    shiftedHead += elementData.size
                    val elementsToShift = internalIndex - head
                    val shiftToBack = elementData.size - shiftedHead

                    if (shiftToBack >= elementsToShift) {
                        elementData.copyInto(elementData, shiftedHead, head, internalIndex)
                    } else {
                        elementData.copyInto(elementData, shiftedHead, head, head + shiftToBack)
                        elementData.copyInto(elementData, 0, head + shiftToBack, internalIndex)
                    }
                }
            } else { // head > tail, internalIndex < tail
                elementData.copyInto(elementData, shiftedHead, head, elementData.size)
                if (elementsSize >= internalIndex) {
                    elementData.copyInto(elementData, elementData.size - elementsSize, 0, internalIndex)
                } else {
                    elementData.copyInto(elementData, elementData.size - elementsSize, 0, elementsSize)
                    elementData.copyInto(elementData, 0, elementsSize, internalIndex)
                }
            }
            head = shiftedHead
            copyCollectionElements(negativeMod(internalIndex - elementsSize), elements)
        } else {
            // closer to the last element -> shift succeeding elements

            val shiftedInternalIndex = internalIndex + elementsSize

            if (internalIndex < tail) {
                if (tail + elementsSize <= elementData.size) {
                    elementData.copyInto(elementData, shiftedInternalIndex, internalIndex, tail)
                } else { // head < tail, insertion leads to head >= tail
                    if (shiftedInternalIndex >= elementData.size) {
                        elementData.copyInto(elementData, shiftedInternalIndex - elementData.size, internalIndex, tail)
                    } else {
                        val shiftToFront = tail + elementsSize - elementData.size
                        elementData.copyInto(elementData, 0, tail - shiftToFront, tail)
                        elementData.copyInto(elementData, shiftedInternalIndex, internalIndex, tail - shiftToFront)
                    }
                }
            } else { // head > tail, internalIndex > head
                elementData.copyInto(elementData, elementsSize, 0, tail)
                if (shiftedInternalIndex >= elementData.size) {
                    elementData.copyInto(
                        elementData,
                        shiftedInternalIndex - elementData.size,
                        internalIndex,
                        elementData.size,
                    )
                } else {
                    elementData.copyInto(elementData, 0, elementData.size - elementsSize, elementData.size)
                    elementData.copyInto(
                        elementData,
                        shiftedInternalIndex,
                        internalIndex,
                        elementData.size - elementsSize,
                    )
                }
            }
            copyCollectionElements(internalIndex, elements)
        }

        return true
    }

    public fun get(index: Int): Int {
        checkElementIndex(index, size)

        return internalGet(internalIndex(index))
    }

    public fun set(
        index: Int,
        element: Int,
    ): Int {
        checkElementIndex(index, size)

        val internalIndex = internalIndex(index)
        val oldElement = internalGet(internalIndex)
        elementData[internalIndex] = element

        return oldElement
    }

    public fun contains(element: Int): Boolean = indexOf(element) != -1

    public fun indexOf(element: Int): Int {
        val tail = internalIndex(size)

        if (head < tail) {
            for (index in head until tail) {
                if (element == elementData[index]) return index - head
            }
        } else {
            for (index in head until elementData.size) {
                if (element == elementData[index]) return index - head
            }
            for (index in 0 until tail) {
                if (element == elementData[index]) return index + elementData.size - head
            }
        }

        return -1
    }

    public fun lastIndexOf(element: Int): Int {
        val tail = internalIndex(size)

        if (head < tail) {
            for (index in tail - 1 downTo head) {
                if (element == elementData[index]) return index - head
            }
        } else if (head > tail) {
            for (index in tail - 1 downTo 0) {
                if (element == elementData[index]) return index + elementData.size - head
            }
            for (index in elementData.lastIndex downTo head) {
                if (element == elementData[index]) return index - head
            }
        }

        return -1
    }

    public fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index == -1) return false
        removeAt(index)
        return true
    }

    public fun removeAt(index: Int): Int {
        checkElementIndex(index, size)

        if (index == lastIndex) {
            return removeLast()
        } else if (index == 0) {
            return removeFirst()
        }

        val internalIndex = internalIndex(index)
        val element = internalGet(internalIndex)

        if (index < size shr 1) {
            // closer to the first element -> shift preceding elements
            if (internalIndex >= head) {
                elementData.copyInto(elementData, head + 1, head, internalIndex)
            } else { // head > tail, internalIndex < head
                elementData.copyInto(elementData, 1, 0, internalIndex)
                elementData[0] = elementData[elementData.size - 1]
                elementData.copyInto(elementData, head + 1, head, elementData.size - 1)
            }

            elementData[head] = 0
            head = incremented(head)
        } else {
            // closer to the last element -> shift succeeding elements
            val internalLastIndex = internalIndex(lastIndex)

            if (internalIndex <= internalLastIndex) {
                elementData.copyInto(elementData, internalIndex, internalIndex + 1, internalLastIndex + 1)
            } else { // head > tail, internalIndex > head
                elementData.copyInto(elementData, internalIndex, internalIndex + 1, elementData.size)
                elementData[elementData.size - 1] = elementData[0]
                elementData.copyInto(elementData, 0, 1, internalLastIndex + 1)
            }

            elementData[internalLastIndex] = 0
        }
        size -= 1

        return element
    }

    public fun removeAll(elements: Collection<Int>): Boolean = filterInPlace { !elements.contains(it) }

    public fun retainAll(elements: Collection<Int>): Boolean = filterInPlace { elements.contains(it) }

    private inline fun filterInPlace(predicate: (Int) -> Boolean): Boolean {
        if (this.isEmpty() || elementData.isEmpty()) {
            return false
        }

        val tail = internalIndex(size)
        var newTail = head
        var modified = false

        if (head < tail) {
            for (index in head until tail) {
                val element = elementData[index]

                if (predicate(element)) {
                    elementData[newTail++] = element
                } else {
                    modified = true
                }
            }

            elementData.fill(0, newTail, tail)
        } else {
            for (index in head until elementData.size) {
                val element = elementData[index]
                elementData[index] = 0

                if (predicate(element)) {
                    elementData[newTail++] = element
                } else {
                    modified = true
                }
            }

            newTail = positiveMod(newTail)

            for (index in 0 until tail) {
                val element = elementData[index]
                elementData[index] = 0

                if (predicate(element)) {
                    elementData[newTail] = element
                    newTail = incremented(newTail)
                } else {
                    modified = true
                }
            }
        }
        if (modified) {
            size = negativeMod(newTail - head)
        }

        return modified
    }

    public fun clear() {
        val tail = internalIndex(size)
        if (head < tail) {
            elementData.fill(0, head, tail)
        } else if (isNotEmpty()) {
            elementData.fill(0, head, elementData.size)
            elementData.fill(0, 0, tail)
        }
        head = 0
        size = 0
    }

    @Suppress("NOTHING_TO_OVERRIDE")
    public fun toArray(array: IntArray): IntArray {
        val dest = (
            if (array.size >= size) {
                array
            } else {
                IntArray(size)
            }
        )

        val tail = internalIndex(size)
        if (head < tail) {
            elementData.copyInto(dest, startIndex = head, endIndex = tail)
        } else if (isNotEmpty()) {
            elementData.copyInto(dest, destinationOffset = 0, startIndex = head, endIndex = elementData.size)
            elementData.copyInto(dest, destinationOffset = elementData.size - head, startIndex = 0, endIndex = tail)
        }

        return array
    }

    @Suppress("NOTHING_TO_OVERRIDE")
    public fun toArray(): IntArray = toArray(IntArray(size))

    internal companion object {
        private val emptyElementData = IntArray(0)
        private const val DEFAULT_MIN_CAPACITY = 10

        internal fun checkElementIndex(
            index: Int,
            size: Int,
        ) {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        internal fun checkPositionIndex(
            index: Int,
            size: Int,
        ) {
            if (index < 0 || index > size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

        /** [oldCapacity] and [minCapacity] must be non-negative. */
        internal fun newCapacity(
            oldCapacity: Int,
            minCapacity: Int,
        ): Int {
            // overflow-conscious
            var newCapacity = oldCapacity + (oldCapacity shr 1)
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
            }
            return newCapacity
        }
    }
}
