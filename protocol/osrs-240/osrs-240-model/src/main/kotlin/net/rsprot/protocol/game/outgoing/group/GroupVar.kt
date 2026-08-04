package net.rsprot.protocol.game.outgoing.group

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.group.util.GroupVarUpdate
import net.rsprot.protocol.message.OutgoingGameMessage

public class GroupVar(
    public val updates: List<GroupVarUpdate<*>>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVar

        if (updates != other.updates) return false

        return true
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "GroupVar(" +
            "updates=$updates" +
            ")"
    }
}
