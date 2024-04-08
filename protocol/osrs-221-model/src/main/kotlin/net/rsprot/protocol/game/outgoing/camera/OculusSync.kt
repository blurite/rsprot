package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.message.OutgoingMessage

/**
 * Oculus sync is used to re-synchronize the orb of oculus
 * camera to the local player in the client, if the value
 * does not match up with the client's value.
 * The client initializes this property as zero.
 * @property value the synchronization value, if the client's
 * value is different, oculus camera is moved to the client's local player.
 * Additionally, this value is sent by the client in the
 * [net.rsprot.protocol.game.incoming.misc.user.Teleport] packet whenever
 * the oculus causes the player to teleport.
 */
public class OculusSync(
    public val value: Int,
) : OutgoingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OculusSync

        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "OculusSync(value=$value)"
    }
}
