package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update UID 192 packed is used to update the random 192-bit
 * id that is found in the random.dat file within the player's
 * cache directory.
 * The 192-bit UID will be accompanied by a 32-bit CRC of the
 * block, which the client will verify before changing the
 * contents of the random.dat file.
 */
public class UpdateUid192(
    public val uid: ByteArray,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateUid192

        return uid.contentEquals(other.uid)
    }

    override fun hashCode(): Int = uid.contentHashCode()

    override fun toString(): String = "UpdateUid192(uid=${uid.contentToString()})"
}
