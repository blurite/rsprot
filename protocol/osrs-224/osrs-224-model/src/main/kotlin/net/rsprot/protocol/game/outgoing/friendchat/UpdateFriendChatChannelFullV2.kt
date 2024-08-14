package net.rsprot.protocol.game.outgoing.friendchat

import net.rsprot.compression.Base37
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update friendchat channel full V2 is used to send full channel updates
 * where the list of entries has a size of more than 255.
 * It can also support sizes below that, but for sizes in range of 128..255,
 * it is more efficient by 1 byte to use V1 of this packet.
 */
public class UpdateFriendChatChannelFullV2(
    public val updateType: UpdateType,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun toString(): String = "UpdateFriendChatChannelFullV2(updateType=$updateType)"

    public sealed interface UpdateType

    /**
     * Join updates are used to enter a friendchat channel.
     * @property channelOwner the name of the player who owns this channel
     * @property channelName the name of the friend chat channel.
     * This name must be compatible with base-37 encoding, meaning
     * it cannot have special symbols, and it must be 12 characters of less.
     * @property kickRank the minimum rank id to kick another player from
     * the friend chat.
     * @property entries the list of friend chat entries to be added.
     */
    public class JoinUpdate(
        override val channelOwner: String,
        public val channelNameBase37: Long,
        private val _kickRank: Byte,
        override val entries: List<FriendChatEntry>,
    ) : UpdateFriendChatChannelFull(),
        UpdateType {
        public constructor(
            channelOwner: String,
            channelName: String,
            kickRank: Int,
            entries: List<FriendChatEntry>,
        ) : this(
            channelOwner,
            Base37.encode(channelName),
            kickRank.toByte(),
            entries,
        )

        override val channelName: String
            get() = Base37.decode(channelNameBase37)
        override val kickRank: Int
            get() = _kickRank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as JoinUpdate

            if (channelOwner != other.channelOwner) return false
            if (channelNameBase37 != other.channelNameBase37) return false
            if (_kickRank != other._kickRank) return false
            if (entries != other.entries) return false

            return true
        }

        override fun hashCode(): Int {
            var result = channelOwner.hashCode()
            result = 31 * result + channelNameBase37.hashCode()
            result = 31 * result + _kickRank.hashCode()
            result = 31 * result + entries.hashCode()
            return result
        }

        override fun toString(): String =
            "UpdateFriendChatChannelFullV2.JoinUpdate(" +
                "channelOwner='$channelOwner', " +
                "channelName='$channelName', " +
                "kickRank=$kickRank, " +
                "entries=$entries" +
                ")"
    }

    /**
     * Leave updates are used to leave a friendchat channel.
     */
    public data object LeaveUpdate : UpdateType {
        override fun toString(): String = "UpdateFriendChatChannelFullV2.LeaveUpdate()"
    }
}
