package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.camera.util.CameraEaseFunction
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam rotate to is used to make the camera look towards
 * an angle relative to the current camera angle.
 * One way to think of this packet is that it **adds** values to the
 * x and y angles of the camera.
 *
 * @property pitch the x angle of the camera to set to.
 * Note that the angle is coerced into a range of 128..383,
 * and incorrectly excludes the third and fifth least significant bits
 * before doing so (by doing [pitch] & 2027, rather than 2047).
 * @property yaw the x angle of the camera to set to.
 * Note that the angle incorrectly excludes the third and fifth least significant bits
 * (by doing [pitch] & 2027, rather than 2047).
 * @property cycles the duration of the movement in client cycles (20ms/cc)
 * @property easing the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class CamRotateTo private constructor(
    private val _pitch: Short,
    private val _yaw: Short,
    private val _cycles: UShort,
    private val _easing: UByte,
) : OutgoingGameMessage {
    public constructor(
        pitch: Int,
        yaw: Int,
        cycles: Int,
        easing: Int,
    ) : this(
        pitch.toShort(),
        yaw.toShort(),
        cycles.toUShort(),
        easing.toUByte(),
    )

    public val pitch: Int
        get() = _pitch.toInt()
    public val yaw: Int
        get() = _yaw.toInt()
    public val cycles: Int
        get() = _cycles.toInt()
    public val easing: CameraEaseFunction
        get() = CameraEaseFunction[_easing.toInt()]
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamRotateTo

        if (_pitch != other._pitch) return false
        if (_yaw != other._yaw) return false
        if (_cycles != other._cycles) return false
        if (_easing != other._easing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _pitch.toInt()
        result = 31 * result + _yaw
        result = 31 * result + _cycles.hashCode()
        result = 31 * result + _easing.hashCode()
        return result
    }

    override fun toString(): String =
        "CamRotateTo(" +
            "pitch=$pitch, " +
            "yaw=$yaw, " +
            "cycles=$cycles, " +
            "easing=$easing" +
            ")"
}
