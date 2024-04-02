package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * A class to hold the values of a given head bar.
 * @param id the id of the headbar to render
 * @param startFill the number of pixels to render of this headbar at in the start.
 * The number of pixels that a headbar supports is defined in its respective headbar config.
 * @param endFill the number of pixels to render of this headbar at in the end,
 * if a [startTime] and [endTime] are defined.
 * @param startTime the delay in client cycles (20ms/cc) until the headbar renders at [startFill]
 * @param endTime the delay in client cycles (20ms/cc) until the headbar arrives at [endFill].
 */
public data class HeadBar(
    public val id: UShort,
    public val startFill: UByte,
    public val endFill: UByte,
    public val endTime: UShort,
    public val startTime: UShort,
) {
    public companion object {
        /**
         * A constant that informs the client to remove the headbar by the given id.
         */
        public const val REMOVED: UShort = 0x7FFFu
    }
}
