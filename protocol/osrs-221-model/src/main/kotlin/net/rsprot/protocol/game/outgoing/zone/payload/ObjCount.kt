package net.rsprot.protocol.game.outgoing.zone.payload

import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProt
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Obj count is a packet used to update the quantity of an obj that's already
 * spawned into the build area. This is only done for objs which are private
 * to a specific user - doing so merges the stacks together into one rather
 * than having two distinct stacks of the same item.
 * @property id the id of the obj to merge
 * @property oldQuantity the old quantity of the obj to find, if no obj
 * by this quantity is found, this packet has no effect client-side
 * @property newQuantity the new quantity to be set to this obj
 * @property xInZone the x coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the obj within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
public class ObjCount private constructor(
    private val _id: UShort,
    public val oldQuantity: Int,
    public val newQuantity: Int,
    private val coordInZone: CoordInZone,
) : ZoneProt, OutgoingGameMessage {
    public constructor(
        id: Int,
        oldQuantity: Int,
        newQuantity: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        oldQuantity,
        newQuantity,
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

    override val protId: Int = ZoneProt.OBJ_COUNT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjCount

        if (_id != other._id) return false
        if (oldQuantity != other.oldQuantity) return false
        if (newQuantity != other.newQuantity) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + oldQuantity
        result = 31 * result + newQuantity
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCount(" +
            "id=$id, " +
            "oldQuantity=$oldQuantity, " +
            "newQuantity=$newQuantity, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
