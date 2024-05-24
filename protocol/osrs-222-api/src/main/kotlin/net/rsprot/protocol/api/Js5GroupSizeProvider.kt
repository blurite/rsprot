package net.rsprot.protocol.api

/**
 * A group size provider for Js5 groups.
 * Returns the size of a group so that it can be prioritized or de-prioritized
 * depending on its size, allowing for a more seamless download of the cache.
 */
public fun interface Js5GroupSizeProvider {
    /**
     * Gets the size of the JS5 group.
     * It should be noted that this function should be efficient in its lookups!
     */
    public fun getSize(
        archive: Int,
        group: Int,
    ): Int
}
