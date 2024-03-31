package net.rsprot.protocol.game.outgoing.info.playerinfo.util

/**
 * A data structure holding information about observer-dependent extended info flags.
 * An example of this would be any extended info blocks that get written when an avatar is
 * moved from low resolution to high resolution, in which case we need to synchronize any
 * data that was set in the past, such as their appearance, the move speed and
 * the face pathingentity status. This additionally includes any extended info blocks which
 * were flagged for a specific observer alone, such as tinting utilized in Tombs of Amascut,
 * where a single user will see tinting applied to all the other members of the party.
 * When setting up the tinting, rather than flagging tinting on the recipient,
 * we flag the observer-dependent flag on the receiver of the given extended info block.
 */
internal class ObserverExtendedInfoFlags(capacity: Int) {
    /**
     * The observer-dependent flags. This array will not include "static" flags.
     */
    private val flags: ByteArray = ByteArray(capacity)

    /**
     * Resets the observer-dependent flags by filling the array with zeros.
     */
    fun reset() {
        flags.fill(0)
    }

    /**
     * Appends the given [flag] for avatar at index [index].
     * @param index the index of the recipient player
     * @param flag the bit flag to enable
     */
    fun addFlag(
        index: Int,
        flag: Int,
    ) {
        flags[index] = (flags[index].toInt() or flag).toByte()
    }

    /**
     * Gets the observer-dependent flag of the avatar at index [index]
     * @param index the index of the recipient player
     * @return the observer-dependent flag value
     */
    fun getFlag(index: Int): Int {
        return flags[index].toInt()
    }
}
