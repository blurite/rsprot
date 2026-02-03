package net.rsprot.protocol.game.outgoing.specific

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
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
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjCountSpecific private constructor(
    private val _id: UShort,
    public val oldQuantity: Int,
    public val newQuantity: Int,
    public val coordGrid: CoordGrid,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        oldQuantity: Int,
        newQuantity: Int,
        coordGrid: CoordGrid,
    ) : this(
        id.toUShort(),
        oldQuantity,
        newQuantity,
        coordGrid,
    )

    public constructor(
        id: Int,
        oldQuantity: Int,
        newQuantity: Int,
        level: Int,
        x: Int,
        z: Int,
    ) : this(
        id.toUShort(),
        oldQuantity,
        newQuantity,
        CoordGrid(level, x, z),
    )

    public val id: Int
        get() = _id.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjCountSpecific

        if (_id != other._id) return false
        if (oldQuantity != other.oldQuantity) return false
        if (newQuantity != other.newQuantity) return false
        if (coordGrid != other.coordGrid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + oldQuantity
        result = 31 * result + newQuantity
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String =
        "ObjCountSpecific(" +
            "id=$id, " +
            "oldQuantity=$oldQuantity, " +
            "newQuantity=$newQuantity, " +
            "coordGrid=$coordGrid" +
            ")"
}
