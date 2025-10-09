package net.rsprot.protocol.game.incoming.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.game.incoming.events.util.MouseMovements
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Mouse move messages are sent when the user moves their mouse across
 * the client.
 * @property stepExcess the extra milliseconds leftover after each mouse movement
 * recording.
 * @property endExcess the extra milliseconds leftover at the end of the packet's tracking.
 * @property movements all the recorded mouse movements within this message.
 * Mouse movements are recorded by the client at a 50 millisecond interval,
 * meaning any movements within that 50 milliseconds are discarded, and
 * only the position changes of the mouse at each 50 millisecond interval
 * are sent.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class EventMouseMove private constructor(
    private val _stepExcess: UByte,
    private val _endExcess: UByte,
    public val movements: MouseMovements,
) : IncomingGameMessage {
    public constructor(
        stepExcess: Int,
        endExcess: Int,
        movements: MouseMovements,
    ) : this(
        stepExcess.toUByte(),
        endExcess.toUByte(),
        movements,
    )

    public val stepExcess: Int
        get() = _stepExcess.toInt()

    public val endExcess: Int
        get() = _endExcess.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun toString(): String =
        "EventMouseMove(" +
            "movements=$movements, " +
            "stepExcess=$stepExcess, " +
            "endExcess=$endExcess" +
            ")"
}
