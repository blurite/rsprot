package net.rsprot.protocol.game.outgoing.zone.payload

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.OldSchoolZoneProt
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInZone
import net.rsprot.protocol.message.ZoneProt

/**
 * Obj opfilter is used to change the right-click options on an obj
 * on the ground. This packet is currently unused in OldSchool RuneScape.
 * It works by finding the first obj in the stack with the provided [id],
 * and modifying the right-click ops on that. It does not verify quantity.
 * @property id the id of the obj that needs to get its ops changed
 * @property opFlags the right-click options to set enabled on that obj.
 * Use the [net.rsprot.protocol.game.outgoing.util.OpFlags] helper object to create these
 * bitpacked values which can be passed into it.
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjOpFilter private constructor(
    private val _id: UShort,
    public val opFlags: Byte,
    private val coordInZone: CoordInZone,
) : ZoneProt {
    public constructor(
        id: Int,
        opFlags: Byte,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        opFlags,
        CoordInZone(xInZone, zInZone),
    )

    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
    override val protId: Int = OldSchoolZoneProt.OBJ_OPFILTER

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjOpFilter

        if (_id != other._id) return false
        if (opFlags != other.opFlags) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + opFlags.hashCode()
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String =
        "ObjOpFilter(" +
            "id=$id, " +
            "opFlags=$opFlags, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
}
