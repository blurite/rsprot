package net.rsprot.protocol.game.outgoing.map

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory

/**
 * Rebuild normal is sent when the game requires a map reload without being in instances.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property worldArea the current world area in which the player resides.
 */
public class RebuildNormalV2 private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    private val _worldArea: UShort,
) : StaticRebuildMessageV2 {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        worldArea: Int,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        worldArea.toUShort(),
    )

    override val zoneX: Int
        get() = _zoneX.toInt()
    override val zoneZ: Int
        get() = _zoneZ.toInt()
    override val worldArea: Int
        get() = _worldArea.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun estimateSize(): Int =
        Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildNormalV2

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (_worldArea != other._worldArea) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + _worldArea.hashCode()
        return result
    }

    override fun toString(): String =
        "RebuildNormalV2(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "worldArea=$worldArea" +
            ")"
}
