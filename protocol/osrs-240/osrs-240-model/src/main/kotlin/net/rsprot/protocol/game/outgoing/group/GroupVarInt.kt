package net.rsprot.protocol.game.outgoing.group

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.group.util.GroupVarUpdate
import net.rsprot.protocol.message.OutgoingGameMessage

public class GroupVarInt(
    public val update: GroupVarUpdate<Int>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVarInt

        return update == other.update
    }

    override fun hashCode(): Int {
        return update.hashCode()
    }

    override fun toString(): String {
        return "GroupVarInt(" +
            "update=$update" +
            ")"
    }
}
