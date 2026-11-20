package net.rsprot.protocol.game.outgoing.group

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.group.util.GroupVariable
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Group full packet is used to perform full updates to a list of groups.
 * The operations can include deleting groups, or adding/updating existing ones.
 * The group definition in the cache determines the length of each variable list
 * and the type of each value. Neither is written to the packet.
 * @property updates the list of group updates to apply.
 */
public class GroupFull(
    public val updates: List<GroupUpdate>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupFull

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "GroupFull(" +
            "updates=$updates" +
            ")"
    }

    public sealed interface GroupUpdate

    /**
     * Deletes the group with [index] in the client's memory.
     */
    public class GroupDelete(
        public val index: Int,
    ) : GroupUpdate {
        init {
            require(index in 0..255) {
                "Index must be in range of 0..255"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GroupDelete

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "GroupDelete(" +
                "index=$index" +
                ")"
        }
    }

    /**
     * Adds or changes an existing group in the client.
     * @property index the index of the group.
     * @property id the id of the group definition in the cache.
     * @property uid the server-tracked uid of the group.
     * @property groupVariables values in the order defined by cache opcode 7.
     * @property groupMemberVariables values in the order defined by cache opcode 8.
     * @property unknownVariables values in the order defined by cache opcode 11.
     */
    public class GroupAddChange(
        public val index: Int,
        public val id: Int,
        public val uid: Long,
        public val groupVariables: List<GroupVariable<*>>,
        public val groupMemberVariables: List<GroupVariable<*>>,
        public val unknownVariables: List<GroupVariable<*>>,
    ) : GroupUpdate {
        public constructor(
            index: Int,
            id: Int,
            uid: Long,
            groupVariables: List<GroupVariable<*>>,
            groupMemberVariables: List<GroupVariable<*>>,
        ) : this(index, id, uid, groupVariables, groupMemberVariables, emptyList())

        init {
            require(index in 0..255) {
                "Index must be in range of 0..255"
            }
            require(id != -1) {
                "Id must not be -1."
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GroupAddChange

            if (index != other.index) return false
            if (id != other.id) return false
            if (uid != other.uid) return false
            if (groupVariables != other.groupVariables) return false
            if (groupMemberVariables != other.groupMemberVariables) return false
            if (unknownVariables != other.unknownVariables) return false

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + id
            result = 31 * result + uid.hashCode()
            result = 31 * result + groupVariables.hashCode()
            result = 31 * result + groupMemberVariables.hashCode()
            result = 31 * result + unknownVariables.hashCode()
            return result
        }

        override fun toString(): String {
            return "GroupAddChange(" +
                "index=$index, " +
                "id=$id, " +
                "uid=$uid, " +
                "groupVariables=$groupVariables, " +
                "groupMemberVariables=$groupMemberVariables, " +
                "unknownVariables=$unknownVariables" +
                ")"
        }
    }
}
