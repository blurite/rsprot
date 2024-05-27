package net.rsprot.protocol.game.outgoing.info.filter

/**
 * A default naive extended info filter. This filter will stop accepting
 * any avatars once 30kb of data has been written in the buffer.
 * As a result of this, it is guaranteed that the packet capacity will
 * never be exceeded under any circumstances, as the remaining 10kb
 * is more than enough to write every extended info block set to the
 * theoretical maximums.
 */
public class DefaultExtendedInfoFilter : ExtendedInfoFilter {
    override fun accept(
        writableBytes: Int,
        constantFlag: Int,
        remainingAvatars: Int,
        previouslyObserved: Boolean,
    ): Boolean {
        return (writableBytes - remainingAvatars) >= THEORETICAL_HIGHEST_EXTENDED_INFO_BLOCK_SIZE
    }

    public companion object {
        /**
         * The theoretical highest is a rough approximation if a player had every extended
         * info block flagged to the maximum, meaning 256 worst case hitmarks, headbars, spotanims,
         * and so on. The real maximum comes to somewhere in the 7,000-8,000 range,
         * however for some head-room and not having to recompute this all the time,
         * we stick with a 10 kilobyte limitation.
         * This limitation is more than enough in just about every scenario in real life.
         * For the longest time, the actual total limitation was only 5 kilobytes for
         * the entire player info packet, so limiting most of it to be 30kb or less
         * is still a huge improvement and should cover almost all realistic scenarios.
         */
        public const val THEORETICAL_HIGHEST_EXTENDED_INFO_BLOCK_SIZE: Int = 10_000
    }
}
