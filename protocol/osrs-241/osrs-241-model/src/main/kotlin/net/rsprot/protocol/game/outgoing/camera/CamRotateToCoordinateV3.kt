package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.camera.util.CameraEaseFunction
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam rotate to coordinate is used to make the camera look towards
 * a certain coordinate with various easing functions.
 *
 * @property x the absolute x coordinate to look at.
 * @property z the absolute z coordinate to look at.
 * @property height the height of the camera
 * @property cycles the duration of the movement in client cycles (20ms/cc)
 * @property easing the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 * @property heightRelative whether the height is relative to the
 * previous camera look-at packet's height.
 * @property trackTarget whether to track the target coordinate,
 * if it moves on the screen.
 */
public class CamRotateToCoordinateV3 private constructor(
    private val _x: UShort,
    private val _z: UShort,
    private val _height: Short,
    private val _cycles: UShort,
    private val _easing: UByte,
    public val heightRelative: Boolean,
    public val trackTarget: Boolean,
) : OutgoingGameMessage {
    public constructor(
        x: Int,
        z: Int,
        height: Int,
        cycles: Int,
        easing: Int,
        heightRelative: Boolean,
        trackTarget: Boolean,
    ) : this(
        x.toUShort(),
        z.toUShort(),
        height.toShort(),
        cycles.toUShort(),
        easing.toUByte(),
        heightRelative,
        trackTarget,
    )

    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
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

        other as CamRotateToCoordinateV3

        if (_height != other._height) return false
        if (heightRelative != other.heightRelative) return false
        if (trackTarget != other.trackTarget) return false
        if (_x != other._x) return false
        if (_z != other._z) return false
        if (_cycles != other._cycles) return false
        if (_easing != other._easing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _height.toInt()
        result = 31 * result + heightRelative.hashCode()
        result = 31 * result + trackTarget.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + _cycles.hashCode()
        result = 31 * result + _easing.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamRotateToCoordinateV3(" +
            "x=$x, " +
            "z=$z, " +
            "height=$height, " +
            "cycles=$cycles, " +
            "easing=$easing, " +
            "heightRelative=$heightRelative, " +
            "trackTarget=$trackTarget" +
            ")"
    }
}
