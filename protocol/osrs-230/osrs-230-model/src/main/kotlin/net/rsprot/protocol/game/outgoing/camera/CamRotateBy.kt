package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.camera.util.CameraEaseFunction
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam rotate by is used to make the camera look towards
 * an angle relative to the current camera angle.
 * One way to think of this packet is that it **adds** values to the
 * x and y angles of the camera.
 *
 * @property pitch the additional angle to add to the x-axis of the camera.
 * It's worth noting that the x angle of the camera ranges between 128 and
 * 383 (inclusive), and the resulting value is coerced in that range.
 * Negative values are also accepted.
 * Additionally, there is currently a bug in the client that causes the
 * third and the fifth least significant bits of the resulting angle to
 * be discarded due to the code doing (cameraXAngle + [pitch] & 2027),
 * which is further coerced into the 128-383 range.
 * @property yaw the additional angle to add to the y-axis of the camera.
 * Unlike the x-axis angle, this one ranges from 0 to 2047 (inclusive),
 * and does not get coerced - instead it will just roll over (e.g. 2047 -> 0).
 * @property cycles the duration of the movement in client cycles (20ms/cc)
 * @property easing the camera easing function, allowing for finer
 * control over the way it moves from the start coordinate to the end.
 */
public class CamRotateBy private constructor(
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

        other as CamRotateBy

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
        "CamRotateBy(" +
            "pitch=$pitch, " +
            "yaw=$yaw, " +
            "cycles=$cycles, " +
            "easing=$easing" +
            ")"
}
