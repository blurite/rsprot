package net.rsprot.protocol.game.incoming.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Mouse move messages are sent when the user moves their mouse across
 * the client.
 * @property totalTime the total time in milliseconds that all the movements
 * inside this event span across
 * @property averageTime the average time in milliseconds between each movement.
 * The average time is truncated according to integer division rules in the JVM.
 * This is equal to `totalTime / count`.
 * @property remainingTime the remaining time from the [averageTime] integer
 * division. This is equal to `totalTime % count`.
 * @property movements all the recorded mouse movements within this message.
 * Mouse movements are recorded by the client at a 50 millisecond interval,
 * meaning any movements within that 50 milliseconds are discarded, and
 * only the position changes of the mouse at each 50 millisecond interval
 * are sent.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class EventMouseMove private constructor(
    private val _averageTime: UByte,
    private val _remainingTime: UByte,
    public val movements: MouseMovements,
) : IncomingGameMessage {
    public constructor(
        averageTime: Int,
        remainingTime: Int,
        movements: MouseMovements,
    ) : this(
        averageTime.toUByte(),
        remainingTime.toUByte(),
        movements,
    )

    public val totalTime: Int
        get() = (_averageTime.toInt() * movements.length) + _remainingTime.toInt()

    public val averageTime: Int
        get() = _averageTime.toInt()

    public val remainingTime: Int
        get() = _remainingTime.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun toString(): String =
        "EventMouseMove(" +
            "movements=$movements, " +
            "totalTime=$totalTime, " +
            "averageTime=$averageTime, " +
            "remainingTime=$remainingTime" +
            ")"

    /**
     * A value class that wraps around an array of mouse movements,
     * with the encoding specified by [MousePosChange].
     * @property length the number of mouse movements in this packet.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    @JvmInline
    public value class MouseMovements(
        private val movements: LongArray,
    ) {
        public val length: Int
            get() = movements.size

        /**
         * @return the mouse movements data structure as a long array,
         * with encoding as specified by [MousePosChange].
         * It is worth noting the encoding does not match up with the client.
         * However, if people wish to store the mouse movements for later usage,
         * this function provides the backing array which can be reconstructed
         * at a later date.
         * Changes to the backing array will directly reflect on this class.
         */
        public fun asLongArray(): LongArray = movements

        /**
         * Gets the mouse position change at the specified [index]
         * @param index the index at which to obtain the mouse pos change.
         * @return the mouse position change that occurred at that index.
         * @throws ArrayIndexOutOfBoundsException if the index is below 0, or >= [length]
         */
        @Throws(ArrayIndexOutOfBoundsException::class)
        public fun getMousePosChange(index: Int): MousePosChange = MousePosChange(movements[index])

        /**
         * A value class for mouse position changes, packed into a primitive long.
         * We utilize bitpacking in order to use primitive long arrays for space
         * constraints.
         * @property packed the bitpacked long value, exposed as servers may wish
         * to re-compose the position changes at a later date.
         * @property timeDelta the time difference in milliseconds since the last
         * transmitted mouse movement.
         * @property xDelta the x coordinate delta of the mouse, in pixels. If the
         * mouse goes outside the client window, the value will be -1.
         * @property yDelta the y coordinate delta of the mouse, in pixels. If the
         * mouse goes outside the client window, the value will be -1.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        @JvmInline
        public value class MousePosChange(
            public val packed: Long,
        ) {
            public constructor(
                timeDelta: Int,
                xDelta: Int,
                yDelta: Int,
            ) : this(
                (timeDelta and 0xFFFF)
                    .toLong()
                    .or(xDelta.toLong() and 0xFFFF shl 16)
                    .or(yDelta.toLong() and 0xFFFF shl 32),
            )

            public val timeDelta: Int
                get() = (packed and 0xFFFF).toInt()
            public val xDelta: Int
                get() = (packed ushr 16 and 0xFFFF).toShort().toInt()
            public val yDelta: Int
                get() = (packed ushr 32 and 0xFFFF).toShort().toInt()

            override fun toString(): String =
                "MousePosChange(" +
                    "timeDelta=$timeDelta, " +
                    "xDelta=$xDelta, " +
                    "yDelta=$yDelta" +
                    ")"
        }
    }
}
