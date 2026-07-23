package org.jire.netty.haproxy

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Decides whether a [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol header
 * received from a directly connected peer will be honored as the "real" source address.
 *
 * Evaluated once per connection, on the event loop, against the peer's transport-level
 * remote address, never against the address claimed inside a PROXY header.
 * Implementations must be cheap and non-blocking.
 */
public fun interface HAProxyTrustPredicate {
    public fun isTrusted(remoteAddress: SocketAddress?): Boolean

    public companion object {
        /**
         * Trusts loopback peers only (`127.0.0.0/8`, `::1`).
         */
        @JvmStatic
        public val LOOPBACK_ONLY: HAProxyTrustPredicate =
            HAProxyTrustPredicate { addr ->
                (addr as? InetSocketAddress)?.address?.isLoopbackAddress == true
            }

        /**
         * Honors the PROXY header from any peer. Spoofable by design, only for networks
         * where every peer is already trusted.
         */
        @JvmStatic
        public val TRUST_ALL: HAProxyTrustPredicate = HAProxyTrustPredicate { true }

        /**
         * Trusts peers matching any of the given entries, each an IP literal with an
         * optional prefix length, e.g. `"10.0.0.5"`, `"10.0.0.0/8"`, `"fd00::/8"`.
         * Hostnames are rejected and address families never cross-match.
         */
        @JvmStatic
        public fun ofCidrs(vararg cidrs: String): HAProxyTrustPredicate {
            require(cidrs.isNotEmpty()) { "At least one CIDR is required" }
            val rules = cidrs.map(::parseCidr)
            return HAProxyTrustPredicate { addr ->
                val inet = (addr as? InetSocketAddress)?.address
                if (inet == null) {
                    false
                } else {
                    val bytes = normalize(inet.address)
                    rules.any { it.matches(bytes) }
                }
            }
        }

        private class CidrRule(
            private val network: ByteArray,
            private val prefixLength: Int,
        ) {
            fun matches(address: ByteArray): Boolean {
                if (address.size != network.size) return false
                val fullBytes = prefixLength ushr 3
                for (i in 0 until fullBytes) {
                    if (address[i] != network[i]) return false
                }
                val remainderBits = prefixLength and 7
                if (remainderBits == 0) return true
                val mask = 0xFF shl (8 - remainderBits) and 0xFF
                return address[fullBytes].toInt() and mask == network[fullBytes].toInt() and mask
            }
        }

        private fun parseCidr(cidr: String): CidrRule {
            val slash = cidr.indexOf('/')
            val literal = if (slash == -1) cidr else cidr.substring(0, slash)
            // InetAddress.getByName resolves hostnames via DNS, only allow IP literals
            require(
                literal.isNotEmpty() &&
                    literal.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '.' || it == ':' },
            ) {
                "Expected an IP literal, got \"$cidr\""
            }
            val network = normalize(InetAddress.getByName(literal).address)
            val maxPrefix = network.size * 8
            val prefix =
                if (slash == -1) {
                    maxPrefix
                } else {
                    val parsed = cidr.substring(slash + 1).toIntOrNull()
                    require(parsed != null && parsed in 0..maxPrefix) {
                        "Invalid prefix length in \"$cidr\""
                    }
                    parsed
                }
            return CidrRule(network, prefix)
        }

        private fun normalize(bytes: ByteArray): ByteArray {
            if (bytes.size != 16) return bytes
            for (i in 0..9) {
                if (bytes[i] != 0.toByte()) return bytes
            }
            if (bytes[10] != 0xFF.toByte() || bytes[11] != 0xFF.toByte()) return bytes
            return bytes.copyOfRange(12, 16)
        }
    }
}
