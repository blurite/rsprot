package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Obj unlock update packets are used to update which objs are unlocked on the client.
 * Each entry replaces the unlock flags for 64 consecutive obj ids. A set bit indicates
 * that the respective obj is unlocked, while an unset bit indicates that it is locked.
 * Entries not included in this packet are left unchanged.
 * @property entries the map of word indices to their unlock flags.
 * The obj id for a given bit is calculated as `wordIndex * 64 + bitIndex`.
 * For example, word index 1 contains the flags for obj ids 64..127.
 */
public class ObjUnlockUpdate(
    public val entries: Map<Int, Long>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    init {
        require(entries.keys.all { it >= 0 })
    }

    override fun estimateSize(): Int = entries.size * 10

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjUnlockUpdate) return false
        if (entries != other.entries) return false
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + entries.hashCode()
        return result
    }

    override fun toString(): String = "ObjUnlockUpdate(entries=$entries)"
}
