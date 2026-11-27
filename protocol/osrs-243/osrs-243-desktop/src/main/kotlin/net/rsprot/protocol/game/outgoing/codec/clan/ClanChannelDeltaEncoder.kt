package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.ClanChannelDelta
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClanChannelDeltaEncoder : MessageEncoder<ClanChannelDelta> {
    override val prot: ServerProt = GameServerProt.CLANCHANNEL_DELTA

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ClanChannelDelta,
    ) {
        buffer.p1(message.clanType)
        buffer.p8(message.clanHash)
        buffer.p8(message.updateNum)
        for (event in message.events) {
            when (event) {
                is ClanChannelDelta.AddUserEvent -> {
                    buffer.p1(1)
                    buffer.p1(255)
                    buffer.pjstrnull(event.name)
                    buffer.p2(event.world)
                    buffer.p1(event.rank)

                    // Unused in all clients, including RS3
                    buffer.p8(0)
                }
                is ClanChannelDelta.DeleteUserEvent -> {
                    buffer.p1(3)
                    buffer.p2(event.index)

                    // Unused in all clients, including RS3
                    buffer.p1(0)
                    buffer.p1(255)
                }
                is ClanChannelDelta.UpdateBaseSettingsEvent -> {
                    buffer.p1(4)
                    val name = event.clanName
                    buffer.pjstrnull(name)
                    if (name != null) {
                        // Unused in all clients, including RS3
                        buffer.p1(0)

                        buffer.p1(event.talkRank)
                        buffer.p1(event.kickRank)
                    }
                }
                is ClanChannelDelta.UpdateUserDetailsEvent -> {
                    buffer.p1(2)
                    buffer.p2(event.index)
                    buffer.p1(event.rank)
                    buffer.p2(event.world)

                    // Unused in all clients, including RS3
                    buffer.p8(0)

                    buffer.pjstr(event.name)
                }
                is ClanChannelDelta.UpdateUserDetailsV2Event -> {
                    buffer.p1(5)

                    // Unused in all clients, including RS3
                    buffer.p1(0)
                    buffer.p2(event.index)
                    buffer.p1(event.rank)
                    buffer.p2(event.world)

                    // Unused in all clients, including RS3
                    buffer.p8(0)

                    buffer.pjstr(event.name)

                    // Unused in all clients, including RS3
                    buffer.p1(0)
                }
            }
        }
        buffer.p1(0)
    }
}
