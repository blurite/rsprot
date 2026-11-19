package net.rsprot.protocol.game.outgoing.specific

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Obj customise is a packet that allows the server to modify an item on the ground, by changing
 * the model, the colours and the textures of it.
 * Unlike [ObjCustomiseSpecificV1], this variant allows multiple colours and textures to be changed,
 * as well as assigning a single colour to the entire model.
 * @property id the id of the obj to update
 * @property quantity the quantity of the obj to update
 * @property model the model id to assign to this obj
 * @property recolours the map of colour indices to the colour values to assign at those indices
 * @property retextures the map of texture indices to the texture values to assign at those indices
 * @property colour the colour value to assign to the entire model, or null if not provided
 * @property coordGrid the absolute coordinate at which the obj is modified.
 */
public class ObjCustomiseSpecificV2(
    public val id: Int,
    public val quantity: Int,
    public val model: Int,
    public val recolours: Map<Int, Int>,
    public val retextures: Map<Int, Int>,
    public val colour: Int?,
    public val coordGrid: CoordGrid,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    init {
        require(id in 0..65535)
        require(model in -1..65534)
        require(recolours.size <= 255 && retextures.size <= 255)
        require(recolours.all { (index, value) -> index in 0..255 && value in 0..65535 })
        require(retextures.all { (index, value) -> index in 0..255 && value in 0..65535 })
        require(colour == null || colour in 0..65535)
    }

    override fun estimateSize(): Int = 15 + (recolours.size + retextures.size) * 3 + if (colour != null) 2 else 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjCustomiseSpecificV2) return false
        if (id != other.id) return false
        if (quantity != other.quantity) return false
        if (model != other.model) return false
        if (recolours != other.recolours) return false
        if (retextures != other.retextures) return false
        if (colour != other.colour) return false
        if (coordGrid != other.coordGrid) return false
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + id.hashCode()
        result = 31 * result + quantity.hashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + recolours.hashCode()
        result = 31 * result + retextures.hashCode()
        result = 31 * result + colour.hashCode()
        result = 31 * result + coordGrid.hashCode()
        return result
    }

    override fun toString(): String =
        "ObjCustomiseSpecificV2(" +
            "id=$id, " +
            "quantity=$quantity, " +
            "model=$model, " +
            "recolours=$recolours, " +
            "retextures=$retextures, " +
            "colour=$colour, " +
            "coordGrid=$coordGrid" +
            ")"
}
