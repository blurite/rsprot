package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.ClanChannelFull
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClanChannelFullEncoder : MessageEncoder<ClanChannelFull> {
    override val prot: ServerProt = GameServerProt.CLANCHANNEL_FULL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ClanChannelFull,
    ) {
        buffer.p1(message.clanType)
        when (val update = message.update) {
            is ClanChannelFull.JoinUpdate -> {
                buffer.p1(update.flags)
                val version = update.version
                if (update.flags and ClanChannelFull.FLAG_HAS_VERSION != 0) {
                    buffer.p1(version)
                }
                buffer.p8(update.clanHash)
                buffer.p8(update.updateNum)
                buffer.pjstr(update.clanName)
                buffer.pboolean(update.discardedBoolean)
                buffer.p1(update.kickRank)
                buffer.p1(update.talkRank)
                val members = update.members
                buffer.p2(members.size)
                val base37 = update.flags and ClanChannelFull.FLAG_USE_BASE_37_NAMES != 0
                val displayNames = update.flags and ClanChannelFull.FLAG_USE_DISPLAY_NAMES != 0
                for (member in members) {
                    if (base37) {
                        buffer.p8(Base37.encode(member.name))
                    }
                    if (displayNames) {
                        buffer.pjstr(member.name)
                    }
                    buffer.p1(member.rank)
                    buffer.p2(member.world)
                    if (version >= 3) {
                        buffer.pboolean(member.discardedBoolean)
                    }
                }
            }
            ClanChannelFull.LeaveUpdate -> {
                // No-op
            }
        }
    }
}
