package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInBuildArea
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam lookat packet is used to make the camera look towards
 * a certain coordinate in the build area.
 * It is important to note that if this is sent together with
 * a map reload, whether this packet comes before or after the
 * map reload makes a difference - as the build area itself changes.
 *
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera
 * @property rate the constant speed at which the camera looks towards
 * to the new coordinate
 * @property rate2 the speed increase as the camera looks
 * towards the end coordinate.
 */
public class CamLookAt private constructor(
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _rate: UByte,
    private val _rate2: UByte,
) : OutgoingGameMessage {
    public constructor(
        xInBuildArea: Int,
        zInBuildArea: Int,
        height: Int,
        rate: Int,
        rate2: Int,
    ) : this(
        CoordInBuildArea(xInBuildArea, zInBuildArea),
        height.toUShort(),
        rate.toUByte(),
        rate2.toUByte(),
    )

    public val destinationXInBuildArea: Int
        get() = destinationCoordInBuildArea.xInBuildArea
    public val destinationZInBuildArea: Int
        get() = destinationCoordInBuildArea.zInBuildArea
    public val height: Int
        get() = _height.toInt()
    public val rate: Int
        get() = _rate.toInt()
    public val rate2: Int
        get() = _rate2.toInt()

    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamLookAt

        if (destinationCoordInBuildArea != other.destinationCoordInBuildArea) return false
        if (_height != other._height) return false
        if (_rate != other._rate) return false
        if (_rate2 != other._rate2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destinationCoordInBuildArea.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _rate.hashCode()
        result = 31 * result + _rate2.hashCode()
        return result
    }

    override fun toString(): String =
        "CamLookAt(" +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "rate=$rate, " +
            "rate2=$rate2" +
            ")"
}
