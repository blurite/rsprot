package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.InetAddressTracker
import java.util.concurrent.ConcurrentHashMap

/**
 * The default tracker for INet addresses, utilizing a concurrent hash map.
 */
public class DefaultInetAddressTracker : InetAddressTracker {
    private val counts: MutableMap<String, Int> = ConcurrentHashMap()

    override fun register(address: String) {
        counts.compute(address) { _, value ->
            (value ?: 0) + 1
        }
    }

    override fun deregister(address: String) {
        counts.compute(address) { _, value ->
            if (value == null || value <= 1) {
                null
            } else {
                value - 1
            }
        }
    }

    override fun getCount(address: String): Int = counts.getOrDefault(address, 0)
}
