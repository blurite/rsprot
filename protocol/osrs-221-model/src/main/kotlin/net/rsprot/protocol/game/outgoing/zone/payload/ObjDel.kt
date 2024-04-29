package net.rsprot.protocol.game.outgoing.zone.payload

import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProt
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Obj del packets are used to delete an existing obj from the build area,
 * assuming it exists in the first place.
 * @property id the id of the obj to delete. Note that the client does bitwise-and
 * on the id to cap it to the lowest 15 bits, meaning the maximum id that can be
 * transmitted is 32767.
 * @property quantity the quantity of the obj to be deleted. If there is no obj
 * with this quantity, nothing will be deleted.
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjDel private constructor(
    private val _id: UShort,
    public val quantity: Int,
    private val coordInZone: CoordInZone,
) : ZoneProt, OutgoingGameMessage {
    public constructor(
        id: Int,
        quantity: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        quantity,
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

    override val protId: Int = ZoneProt.OBJ_DEL

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjDel

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjDel(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
