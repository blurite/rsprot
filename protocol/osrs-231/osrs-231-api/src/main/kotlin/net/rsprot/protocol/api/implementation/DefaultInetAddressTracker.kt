package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.InetAddressTracker
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * The default tracker for INet addresses, utilizing a concurrent hash map.
 */
public class DefaultInetAddressTracker : InetAddressTracker {
    private val counts: MutableMap<SocketAddress, Int> = ConcurrentHashMap()

    override fun register(address: SocketAddress) {
        counts.compute(address) { _, value ->
            (value ?: 0) + 1
        }
    }

    override fun deregister(address: SocketAddress) {
        counts.compute(address) { _, value ->
            if (value == null || value <= 1) {
                null
            } else {
                value - 1
            }
        }
    }

    override fun getCount(address: SocketAddress): Int = counts.getOrDefault(address, 0)
}
