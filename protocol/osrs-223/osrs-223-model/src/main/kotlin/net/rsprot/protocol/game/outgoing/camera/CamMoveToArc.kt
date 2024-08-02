package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.camera.util.CameraEaseFunction
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInBuildArea
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Camera move to arc packet is used to move camera
 * to a new coordinate with finer control behind it.
 * This packet differs from [CamMoveToCycles] in that it will first
 * move through a center coordinate before going towards the destination,
 * creating a `)`-shape movement. An example image of this can be seen
 * [here](https://media.z-kris.com/2024/04/cam%20move%20eased%20circular.png)
 *
 * @property centerXInBuildArea the center x coordinate within the build area,
 * in range of 0 to 103 (inclusive). This marks the middle point between the
 * camera movement through which the camera has to go.
 * @property centerZInBuildArea the center z coordinate within the build area,
 * in range of 0 to 103 (inclusive). This marks the middle point between the
 * camera movement through which the camera has to go.
 * @property destinationXInBuildArea the dest x coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property destinationZInBuildArea the dest z coordinate within the build area,
 * in range of 0 to 103 (inclusive)
 * @property height the height of the camera once it arrives at the destination
 * @property cycles the duration of the movement in client cycles (20ms/cc)
 * @property ignoreTerrain whether the camera moves along the terrain,
 * moving up and down according to bumps in the terrain.
 * If true, the camera will move in a straight line from the starting position
 * towards the end position, ignoring any changes in the terrain.
 * @property easing the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
@Suppress("DuplicatedCode")
public class CamMoveToArc private constructor(
    private val centerCoordInBuildArea: CoordInBuildArea,
    private val destinationCoordInBuildArea: CoordInBuildArea,
    private val _height: UShort,
    private val _cycles: UShort,
    public val ignoreTerrain: Boolean,
    private val _easing: UByte,
) : OutgoingGameMessage {
    public constructor(
        centerXInBuildArea: Int,
        centerZInBuildArea: Int,
        destinationXInBuildArea: Int,
        destinationZInBuildArea: Int,
        height: Int,
        cycles: Int,
        ignoreTerrain: Boolean,
        easing: Int,
    ) : this(
        CoordInBuildArea(centerXInBuildArea, centerZInBuildArea),
        CoordInBuildArea(destinationXInBuildArea, destinationZInBuildArea),
        height.toUShort(),
        cycles.toUShort(),
        ignoreTerrain,
        easing.toUByte(),
    )

    public val centerXInBuildArea: Int
        get() = centerCoordInBuildArea.xInBuildArea
    public val centerZInBuildArea: Int
        get() = centerCoordInBuildArea.zInBuildArea
    public val destinationXInBuildArea: Int
        get() = destinationCoordInBuildArea.xInBuildArea
    public val destinationZInBuildArea: Int
        get() = destinationCoordInBuildArea.zInBuildArea
    public val height: Int
        get() = _height.toInt()
    public val cycles: Int
        get() = _cycles.toInt()
    public val easing: CameraEaseFunction
        get() = CameraEaseFunction[_easing.toInt()]
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMoveToArc

        if (centerCoordInBuildArea != other.centerCoordInBuildArea) return false
        if (destinationCoordInBuildArea != other.destinationCoordInBuildArea) return false
        if (_height != other._height) return false
        if (_cycles != other._cycles) return false
        if (ignoreTerrain != other.ignoreTerrain) return false
        if (_easing != other._easing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = centerCoordInBuildArea.hashCode()
        result = 31 * result + destinationCoordInBuildArea.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + _cycles.hashCode()
        result = 31 * result + ignoreTerrain.hashCode()
        result = 31 * result + _easing.hashCode()
        return result
    }

    override fun toString(): String =
        "CamMoveToArc(" +
            "centerXInBuildArea=$centerXInBuildArea, " +
            "centerZInBuildArea=$centerZInBuildArea, " +
            "destinationXInBuildArea=$destinationXInBuildArea, " +
            "destinationZInBuildArea=$destinationZInBuildArea, " +
            "height=$height, " +
            "cycles=$cycles, " +
            "ignoreTerrain=$ignoreTerrain, " +
            "easing=$easing" +
            ")"
}
