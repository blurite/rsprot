package org.jire.netty.haproxy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HAProxyTrustPredicateTest {
    private fun addr(ip: String): InetSocketAddress = InetSocketAddress(InetAddress.getByName(ip), 43594)

    @Test
    fun `loopback only trusts loopback`() {
        val predicate = HAProxyTrustPredicate.LOOPBACK_ONLY
        assertTrue(predicate.isTrusted(addr("127.0.0.1")))
        assertTrue(predicate.isTrusted(addr("127.53.2.1")))
        assertTrue(predicate.isTrusted(addr("::1")))
        assertFalse(predicate.isTrusted(addr("8.8.8.8")))
        assertFalse(predicate.isTrusted(null))
    }

    @Test
    fun `trust all trusts anything`() {
        assertTrue(HAProxyTrustPredicate.TRUST_ALL.isTrusted(addr("8.8.8.8")))
        assertTrue(HAProxyTrustPredicate.TRUST_ALL.isTrusted(null))
    }

    @ParameterizedTest
    @CsvSource(
        "10.0.0.0/8, 10.255.255.255, true",
        "10.0.0.0/8, 11.0.0.0, false",
        "10.0.0.0/12, 10.15.255.255, true",
        "10.0.0.0/12, 10.16.0.0, false",
        "10.0.0.4/31, 10.0.0.5, true",
        "10.0.0.4/31, 10.0.0.6, false",
        "10.0.0.5/32, 10.0.0.5, true",
        "10.0.0.5/32, 10.0.0.4, false",
        "10.0.0.5, 10.0.0.5, true",
        "10.0.0.5, 10.0.0.6, false",
        "0.0.0.0/0, 203.0.113.7, true",
        "fd00::/8, fd12:3456::1, true",
        "fd00::/8, fe80::1, false",
        "'::1', '::1', true",
        "'::/0', 'fe80::1', true",
    )
    fun `cidr matching`(
        cidr: String,
        peer: String,
        expected: Boolean,
    ) {
        val predicate = HAProxyTrustPredicate.ofCidrs(cidr)
        if (expected) {
            assertTrue(predicate.isTrusted(addr(peer)))
        } else {
            assertFalse(predicate.isTrusted(addr(peer)))
        }
    }

    @Test
    fun `families never cross-match`() {
        assertFalse(HAProxyTrustPredicate.ofCidrs("0.0.0.0/0").isTrusted(addr("fe80::1")))
        assertFalse(HAProxyTrustPredicate.ofCidrs("::/0").isTrusted(addr("8.8.8.8")))
    }

    @Test
    fun `ipv4-mapped ipv6 is normalized on both sides`() {
        assertTrue(HAProxyTrustPredicate.ofCidrs("10.0.0.5/32").isTrusted(addr("::ffff:10.0.0.5")))
        assertTrue(HAProxyTrustPredicate.ofCidrs("::ffff:10.0.0.5").isTrusted(addr("10.0.0.5")))
        assertFalse(HAProxyTrustPredicate.ofCidrs("10.0.0.5/32").isTrusted(addr("::ffff:10.0.0.6")))
    }

    @Test
    fun `any matching entry trusts the peer`() {
        val predicate = HAProxyTrustPredicate.ofCidrs("192.168.0.0/16", "10.0.0.5")
        assertTrue(predicate.isTrusted(addr("192.168.4.20")))
        assertTrue(predicate.isTrusted(addr("10.0.0.5")))
        assertFalse(predicate.isTrusted(addr("10.0.0.6")))
    }

    @Test
    fun `invalid input is rejected`() {
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs() }
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs("proxy.internal") }
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs("10.0.0.0/33") }
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs("10.0.0.0/-1") }
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs("10.0.0.0/") }
        assertThrows<IllegalArgumentException> { HAProxyTrustPredicate.ofCidrs("/8") }
    }
}
