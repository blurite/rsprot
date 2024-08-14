package net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.util

/**
 * A class to hold the values of a given head bar.
 * @param sourceIndex the index of the entity that dealt the hit that resulted in this headbar.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * If there is no source, set the index to -1.
 * The index will be used for rendering purposes, as both the player who dealt
 * the hit, and the recipient will see the [selfType] variant, and everyone else
 * will see the [otherType] variant, which, if set to -1 will be skipped altogether.
 * @param selfType the id of the headbar to render to the entity on which the headbar appears,
 * as well as the source who resulted in the creation of the headbar.
 * @param otherType the id of the headbar to render to everyone that doesn't fit the [selfType]
 * criteria. If set to -1, the headbar will not be rendered to these individuals.
 * @param startFill the number of pixels to render of this headbar at in the start.
 * The number of pixels that a headbar supports is defined in its respective headbar config.
 * @param endFill the number of pixels to render of this headbar at in the end,
 * if a [startTime] and [endTime] are defined.
 * @param startTime the delay in client cycles (20ms/cc) until the headbar renders at [startFill]
 * @param endTime the delay in client cycles (20ms/cc) until the headbar arrives at [endFill].
 */
public data class HeadBar(
    public var sourceIndex: Int,
    public var selfType: UShort,
    public var otherType: UShort,
    public val startFill: UByte,
    public val endFill: UByte,
    public val startTime: UShort,
    public val endTime: UShort,
) {
    public companion object {
        /**
         * A constant that informs the client to remove the headbar by the given id.
         */
        public const val REMOVED: UShort = 0x7FFFu
    }
}
