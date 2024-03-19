package net.rsprot.protocol.filter

/**
 * A dropping message filter implementation.
 *
 * All incoming messages will be dropped after [threshold]
 * amount of them has been received in one cycle.
 *
 * [threshold] must be an integer between 1 and 255 (inclusive),
 * as this data structure uses unsigned bytes to keep track of counts.
 */
@ExperimentalUnsignedTypes
public class DroppingMessageFilter(
    private val threshold: Int = DEFAULT_MESSAGE_THRESHOLD,
) : MessageFilter {
    init {
        require(threshold in 1..UByte.MAX_VALUE.toInt()) {
            "Message capacity must be between 1 and ${UByte.MAX_VALUE}."
        }
    }

    private val messageCounts: UByteArray = UByteArray(256)

    override fun reset() {
        messageCounts.fill(0u)
    }

    override fun accept(
        id: Int,
        size: Int,
    ): Int {
        val count = messageCounts[id].toInt()
        if (count >= threshold) {
            return MessageFilter.DROP_MESSAGE
        }
        messageCounts[id] = (count + 1).toUByte()
        return MessageFilter.ACCEPT_MESSAGE
    }

    private companion object {
        private const val DEFAULT_MESSAGE_THRESHOLD = 10
    }
}
