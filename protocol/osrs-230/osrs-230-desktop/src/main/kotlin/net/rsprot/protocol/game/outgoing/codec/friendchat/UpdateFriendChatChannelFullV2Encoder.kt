package net.rsprot.protocol.game.outgoing.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.friendchat.UpdateFriendChatChannelFullV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateFriendChatChannelFullV2Encoder : MessageEncoder<UpdateFriendChatChannelFullV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateFriendChatChannelFullV2,
    ) {
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV2.JoinUpdate -> {
                buffer.pjstr(update.channelOwner)
                buffer.p8(update.channelNameBase37)
                buffer.p1(update.kickRank)
                buffer.pSmart1or2null(update.entries.size)
                for (entry in update.entries) {
                    buffer.pjstr(entry.name)
                    buffer.p2(entry.worldId)
                    buffer.p1(entry.rank)
                    buffer.pjstr(entry.worldName)
                }
            }
            UpdateFriendChatChannelFullV2.LeaveUpdate -> {
                // No-op, empty packet
            }
        }
    }
}
