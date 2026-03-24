package net.rsprot.protocol.game.outgoing.specific

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Obj uncustomise resets any customisations done to an obj via the [ObjCustomiseSpecific] packet.
 * @property id the id of the obj to update
 * @property quantity the quantity of the obj to update
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjUncustomiseSpecific private constructor(
    private val _id: UShort,
    public val quantity: Int,
    public val coordGrid: CoordGrid,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        quantity: Int,
        coordGrid: CoordGrid,
    ) : this(
        id.toUShort(),
        quantity,
        coordGrid,
    )

    public constructor(
        id: Int,
        quantity: Int,
        level: Int,
        x: Int,
        z: Int,
    ) : this(
        id.toUShort(),
        quantity,
        CoordGrid(level, x, z),
    )

    public val id: Int
        get() = _id.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjUncustomiseSpecific) return false

        if (_id != other._id) return false
        if (quantity != other.quantity) return false
        if (coordGrid != other.coordGrid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjCustomiseSpecific(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "coordGrid=$coordGrid" +
            ")"
    }
}
