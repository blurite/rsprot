package net.rsprot.protocol.game.outgoing.map

import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.game.outgoing.map.util.XteaProvider
import net.rsprot.protocol.message.OutgoingMessage

/**
 * Rebuild normal is sent when the game requires a map reload without being in instances.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property keys the list of xtea keys needed to decrypt the map.
 */
public class RebuildNormal private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    public val keys: List<XteaKey>,
) : OutgoingMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        keyProvider: XteaProvider,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        buildXteaKeyList(zoneX, zoneZ, keyProvider),
    )

    public val zoneX: Int
        get() = _zoneX.toInt()
    public val zoneZ: Int
        get() = _zoneZ.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildNormal

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (keys != other.keys) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + keys.hashCode()
        return result
    }

    override fun toString(): String {
        return "RebuildNormal(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "keys=$keys" +
            ")"
    }

    private companion object {
        /**
         * A helper function to build the mapsquare key list the same way the client does,
         * as the keys must be in the same specific order as the client reads it.
         */
        private fun buildXteaKeyList(
            zoneX: Int,
            zoneZ: Int,
            keyProvider: XteaProvider,
        ): List<XteaKey> {
            val minMapsquareX = (zoneX - 6) ushr 3
            val maxMapsquareX = (zoneX + 6) ushr 3
            val minMapsquareZ = (zoneZ - 6) ushr 3
            val maxMapsquareZ = (zoneZ + 6) ushr 3
            val count = (maxMapsquareX - minMapsquareZ + 1) * (maxMapsquareZ - minMapsquareZ + 1)
            val keys = ArrayList<XteaKey>(count)
            for (mapsquareX in minMapsquareX..maxMapsquareX) {
                for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                    keys += keyProvider.provide((mapsquareX shl 8) or mapsquareZ)
                }
            }
            return keys
        }
    }
}
