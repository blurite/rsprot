package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Cam mode is used to set the camera into an orb-of-oculus mode,
 * or out of it.
 * @property mode the mode to set in, with the only valid values being
 * 0 for "out of oculus" and 1 for "into oculus".
 */
public class CamMode(
    public val mode: Int,
) : OutgoingGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMode

        return mode == other.mode
    }

    override fun hashCode(): Int {
        return mode
    }

    override fun toString(): String {
        return "CamMode(mode=$mode)"
    }
}
