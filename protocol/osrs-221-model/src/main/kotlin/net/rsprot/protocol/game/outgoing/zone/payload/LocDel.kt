package net.rsprot.protocol.game.outgoing.zone.payload

import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInZone
import net.rsprot.protocol.game.outgoing.zone.payload.util.LocProperties
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProt

/**
 * Loc del packets are used to delete locs from the world.
 * @property xInZone the x coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property shape the shape of the loc, a value of 0 to 22 (inclusive) is expected.
 * @property rotation the rotation of the loc, a value of 0 to 3 (inclusive) is expected.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class LocDel private constructor(
    private val coordInZone: CoordInZone,
    private val locProperties: LocProperties,
) : ZoneProt {
    public constructor(
        xInZone: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
    ) : this(
        CoordInZone(xInZone, zInZone),
        LocProperties(shape, rotation),
    )

    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone
    public val shape: Int
        get() = locProperties.shape
    public val rotation: Int
        get() = locProperties.rotation

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    public val locPropertiesPacked: Int
        get() = locProperties.packed.toInt()

    override val protId: Int = ZoneProt.LOC_DEL

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocDel

        if (coordInZone != other.coordInZone) return false
        if (locProperties != other.locProperties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coordInZone.hashCode()
        result = 31 * result + locProperties.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocDel(" +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation" +
            ")"
    }
}