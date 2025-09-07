package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.loginprot.incoming.RemainingBetaArchives
import java.util.concurrent.ConcurrentHashMap

/**
 * A JS5 authorizer that utilizes a concurrent hashmap to keep track of how many times
 * an [String] has been authorized, to allow multiple clients to keep downloading
 * the cache, if necessary.
 * Furthermore, utilizes a [Long] bitmask of [protectedArchives] for performant authorization
 * validation.
 *
 * @param protectedArchives the list of protected archive ids which require authorization
 * to download the groups for.
 */
public class ConcurrentJs5Authorizer(
    protectedArchives: List<Int>,
) : Js5Authorizer {
    public constructor() : this(RemainingBetaArchives.protectedArchives)

    private val counts = ConcurrentHashMap<String, Int>(DEFAULT_CAPACITY)
    private val protectedArchivesBitMask: Long = buildProtectedArchivesBitMask(protectedArchives)

    private fun buildProtectedArchivesBitMask(protectedArchives: List<Int>): Long {
        return protectedArchives.fold(0L) { acc, value ->
            acc or (1L shl value)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isProtected(archive: Int): Boolean {
        return protectedArchivesBitMask and (1L shl archive) != 0L
    }

    override fun authorize(address: String) {
        try {
            counts.compute(address) { _, old ->
                if (old != null) {
                    old + 1
                } else {
                    // Validation in case there is some logic flaw that results in addresses not getting
                    // cleaned up when they should - in which case just begin rejecting any future ones.
                    // Note that this isn't atomic, it's just a rough limit, we don't care for precise amounts.
                    if (counts.size >= MAXIMUM_CAPACITY) {
                        logger.error {
                            "Authorized JS5 addresses has reached $MAXIMUM_CAPACITY entries - possible memory leak?"
                        }
                        null
                    } else {
                        1
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to authorize $address"
            }
        }
    }

    override fun unauthorize(address: String) {
        try {
            counts.compute(address) { _, old ->
                when {
                    old == null || old <= 1 -> null
                    else -> old - 1
                }
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to unauthorize $address"
            }
        }
    }

    override fun isAuthorized(
        address: String,
        archive: Int,
    ): Boolean {
        return try {
            !isProtected(archive) || isAuthorized(address)
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to check for authorization: $archive @ $address"
            }
            false
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isAuthorized(address: String): Boolean {
        val count = counts[address]
        return count != null && count > 0
    }

    override fun toString(): String {
        return "ConcurrentJs5Authorizer(" +
            "protectedArchivesBitMask=$protectedArchivesBitMask, " +
            "counts=$counts" +
            ")"
    }

    private companion object {
        private const val MAXIMUM_CAPACITY: Int = 1_000_000
        private const val DEFAULT_CAPACITY: Int = 2048
        private val logger: InlineLogger = InlineLogger()
    }
}
