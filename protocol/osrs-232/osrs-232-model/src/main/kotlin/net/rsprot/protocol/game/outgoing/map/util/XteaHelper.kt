package net.rsprot.protocol.game.outgoing.map.util

import net.rsprot.crypto.xtea.XteaKey

/**
 * A helper function to build the mapsquare key list the same way the client does,
 * as the keys must be in the same specific order as the client reads it.
 */
internal fun buildXteaKeyList(
    zoneX: Int,
    zoneZ: Int,
    keyProvider: XteaProvider,
): List<XteaKey> {
    val minMapsquareX = (zoneX - 6).coerceAtLeast(0) ushr 3
    val maxMapsquareX = (zoneX + 6).coerceAtMost(2047) ushr 3
    val minMapsquareZ = (zoneZ - 6).coerceAtLeast(0) ushr 3
    val maxMapsquareZ = (zoneZ + 6).coerceAtMost(2047) ushr 3
    val count = (maxMapsquareX - minMapsquareX + 1) * (maxMapsquareZ - minMapsquareZ + 1)
    val keys = ArrayList<XteaKey>(count.coerceIn(4, 9))
    for (mapsquareX in minMapsquareX..maxMapsquareX) {
        for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
            keys += keyProvider.provide((mapsquareX shl 8) or mapsquareZ)
        }
    }
    return keys
}
