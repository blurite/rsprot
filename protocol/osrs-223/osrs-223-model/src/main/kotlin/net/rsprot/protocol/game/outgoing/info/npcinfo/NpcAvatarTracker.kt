package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLongArray

/**
 * A class that allows us to check which players are currently observing each NPC.
 * Servers sometimes rely on checking the players that currently observe a NPC when
 * determining things like aggression. This implementation makes migrating to RSProt easier.
 *
 * We additionally use this class to track dormant NPCs, allowing us to skip computations for
 * NPCs that do not have any players observing them, which lets us skip potentially thousands
 * of calculations each cycle.
 */
public class NpcAvatarTracker {
    /**
     * The number of player avatars observing this NPC avatar.
     * We utilize the count tracking to determine what NPCs require precomputation.
     * As the game has circa 25,000 NPCs, and even at max world capacity, only 2,000 players,
     * the majority of NPCs in the game will at all times __not__ be observed by any players.
     * This means computing their high resolution blocks is unnecessary, as that is strictly
     * only for players who are already observing a NPC - moving from low resolution to high
     * resolution has its own set of code.
     * Additionally, this is used to skip computing extended info blocks later on in the cycle,
     * given the assumption that no player added this NPC to their high resolution view.
     * Furthermore, this observer count must be an atomic integer, as certain parts of NPC info
     * are multithreaded, including the parts which modify this count.
     */
    private val counter: AtomicInteger = AtomicInteger(0)

    /**
     * A bit set of all the player indices that are currently observing this avatar.
     * Each bit that is set to true here corresponds to the index of the player that
     * is observing that NPC.
     */
    private val observingPlayers: AtomicLongArray = AtomicLongArray(LONGS_IN_USE)

    /**
     * A cached read-only Int set providing easy view over all the players observing
     * this NPC's avatar.
     */
    private val cachedSet: AvatarSet = AvatarSet()

    /**
     * Adds the player with the specified [index] to this bit set if it doesn't already exist.
     * @param index the index of the player to add to this set.
     */
    public fun add(index: Int) {
        if (setObservingPlayer(index)) {
            counter.incrementAndGet()
        }
    }

    /**
     * Removes a player with the specified [index] from this bit set if it exists.
     * @param index the index of the player to remove from this set.
     */
    public fun remove(index: Int) {
        if (unsetObservingPlayer(index)) {
            counter.decrementAndGet()
        }
    }

    /**
     * Returns the cached avatar set of all the indices of player avatars that this
     * NPC avatar is being observed by.
     * @return a set of all the player indices observing this NPC.
     */
    public fun getCachedSet(): AvatarSet = cachedSet

    /**
     * Resets all the tracking metrics for this avatar tracker.
     */
    public fun reset() {
        counter.set(0)
        for (i in 0..<LONGS_IN_USE) {
            observingPlayers.set(i, 0)
        }
    }

    /**
     * Checks whether this NPC avatar has any players currently observing it.
     */
    public fun hasObservers(): Boolean = counter.get() > 0

    /**
     * Gets the current number of players observing this avatar.
     */
    public fun getObserverCount(): Int = counter.get()

    /**
     * Checks whether the player at the specified [index] is currently observing
     * this NPC avatar.
     * @param index the index of the player to check.
     */
    private fun isObservingPlayer(index: Int): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        return this.observingPlayers[longIndex] and bit != 0L
    }

    /**
     * Marks the player at index [index] as observing this NPC.
     * @param index the index of the player to mark as observing this NPC.
     * @return true if the player at index [index] was not already observing this NPC, false if it was.
     */
    private fun setObservingPlayer(index: Int): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        while (true) {
            val cur = this.observingPlayers[longIndex]
            val assigned =
                this.observingPlayers.weakCompareAndSetVolatile(
                    longIndex,
                    cur,
                    cur or bit,
                )
            if (!assigned) continue
            return (cur ushr (index and 0x3F) and 0x1) == 0L
        }
    }

    /**
     * Unmarks the player at index [index] as observing this NPC.
     * @param index the index of the player to unmark as observing this NPC.
     * @return true if the player was previously observing this NPC, false if not.
     */
    private fun unsetObservingPlayer(index: Int): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        while (true) {
            val cur = this.observingPlayers[longIndex]
            val assigned =
                this.observingPlayers.weakCompareAndSetVolatile(
                    longIndex,
                    cur,
                    cur and bit.inv(),
                )
            if (!assigned) continue
            return (cur ushr (index and 0x3F) and 0x1) != 0L
        }
    }

    override fun toString(): String =
        "NpcAvatarTracker(" +
            "counter=$counter, " +
            "observingPlayers=$observingPlayers" +
            ")"

    /**
     * A Set implementation to provide easy access over all the player avatars observing
     * this NPC.
     * Note that only a single instance per NPC is ever created, meaning this should not be
     * stored for long-term use. The iterator of this set will throw a concurrent modification
     * exception if it is accessed across multiple game cycles.
     *
     * Furthermore, this set does not preserve iteration order, but it does ensure an ascending
     * order of indices, allowing for potential use of features like
     * [Binary Search](https://en.wikipedia.org/wiki/Binary_search)
     */
    public inner class AvatarSet : Set<Int> {
        override val size: Int
            get() = counter.get()

        override fun contains(element: Int): Boolean {
            if (element < 0 || element >= PROTOCOL_CAPACITY) {
                return false
            }
            return isObservingPlayer(element)
        }

        override fun containsAll(elements: Collection<Int>): Boolean {
            if (elements.isEmpty()) {
                return true
            }
            for (element in elements) {
                if (!contains(element)) {
                    return false
                }
            }
            return true
        }

        override fun isEmpty(): Boolean = size == 0

        override fun iterator(): Iterator<Int> = AvatarSetIterator(NpcInfoProtocol.cycleCount)

        override fun toString(): String =
            buildString {
                append("[")
                for (element in this@AvatarSet) {
                    append(element).append(", ")
                }
                if (isNotEmpty()) {
                    delete(length - 2, length)
                }
                append("]")
            }

        /**
         * An iterator implementation of this avatar set.
         * @property cycle the cycle at which the iterator was created.
         * If the cycle does not align up with [NpcInfoProtocol.cycleCount],
         * a [ConcurrentModificationException] will be thrown when trying to call any of the functions.
         * @property next the index of the next element in the set. We store a property here to
         * avoid doing double checks every time we wish to advance the iterator.
         * @property searchStartIndex the index at which to begin searching for the next element.
         */
        private inner class AvatarSetIterator(
            private val cycle: Int,
        ) : Iterator<Int> {
            private var next: Int = NO_NEXT_CHECKED
            private var searchStartIndex: Int = 0

            override fun hasNext(): Boolean {
                checkConcurrentModification()
                if (next == NO_NEXT_CHECKED) {
                    setNextNodeIndex()
                }
                return next != NO_NEXT_SET
            }

            /**
             * Finds the next observing player index and sets it to the [next] property.
             * If no result is found, [NO_NEXT_SET] will be assigned instead.
             */
            private fun setNextNodeIndex() {
                var longIndex = searchStartIndex ushr 6
                if (longIndex >= LONGS_IN_USE) {
                    this.next = NO_NEXT_SET
                    return
                }
                var slice = observingPlayers[longIndex] and (LONG_MASK shl searchStartIndex)
                while (true) {
                    if (slice != 0L) {
                        this.next = (longIndex * Long.SIZE_BITS) + java.lang.Long.numberOfTrailingZeros(slice)
                        this.searchStartIndex = this.next + 1
                        return
                    }
                    if (++longIndex == LONGS_IN_USE) {
                        this.next = NO_NEXT_SET
                        return
                    }
                    slice = observingPlayers[longIndex]
                }
            }

            override fun next(): Int {
                checkConcurrentModification()
                if (next == NO_NEXT_CHECKED) {
                    setNextNodeIndex()
                }
                val next = this.next
                if (next == NO_NEXT_SET) {
                    throw NoSuchElementException()
                }
                this.next = NO_NEXT_CHECKED
                return next
            }

            /**
             * Checks for concurrent modifications via ensuring the cycle count still matches up.
             * The intent here is that the iterator should not be accessed across multiple cycles,
             * as the contents of this bit set are likely to change and be invalid.
             */
            private fun checkConcurrentModification() {
                if (cycle != NpcInfoProtocol.cycleCount) {
                    throw ConcurrentModificationException(
                        "Npc avatar iterator cannot be accessed across cycles.",
                    )
                }
            }
        }
    }

    private companion object {
        /**
         * A constant value indicating that no next index has been searched yet in the iterator,
         * implying the next one should be seeker before determining the iterator has finished.
         */
        private const val NO_NEXT_CHECKED: Int = -2

        /**
         * A constant value indicating that there are no more elements left to iterate over.
         */
        private const val NO_NEXT_SET: Int = -1

        /**
         * The constant flag that has all bits in a long enabled.
         */
        private const val LONG_MASK: Long = -1L

        /**
         * The number of longs in use to match our 2048 player indices threshold.
         */
        private const val LONGS_IN_USE: Int = PROTOCOL_CAPACITY ushr 6
    }
}
