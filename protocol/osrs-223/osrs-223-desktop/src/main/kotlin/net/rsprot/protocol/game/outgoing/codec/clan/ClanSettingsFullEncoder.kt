package net.rsprot.protocol.game.outgoing.codec.clan

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.ClanSettingsFull
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClanSettingsFullEncoder : MessageEncoder<ClanSettingsFull> {
    override val prot: ServerProt = GameServerProt.CLANSETTINGS_FULL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: ClanSettingsFull,
    ) {
        buffer.p1(message.clanType)
        val update = message.update
        when (update) {
            is ClanSettingsFull.JoinUpdate -> {
                // Send version always as 6, as it contains the most information
                buffer.p1(6)
                buffer.p1(update.flags)
                buffer.p4(update.updateNum)
                buffer.p4(update.creationTime)
                buffer.p2(update.affinedMembers.size)
                buffer.p1(update.bannedMembers.size)
                buffer.pjstr(update.clanName)

                // Unused in all clients, including RS3
                buffer.p4(0)
                buffer.p1(if (update.allowUnaffined) 1 else 0)
                buffer.p1(update.talkRank)
                buffer.p1(update.kickRank)
                buffer.p1(update.lootshareRank)
                buffer.p1(update.coinshareRank)
                val hasAffinedHashes = update.flags and ClanSettingsFull.FLAG_HAS_AFFINED_HASHES != 0
                val hasAffinedDisplayNames = update.flags and ClanSettingsFull.FLAG_HAS_AFFINED_DISPLAY_NAMES != 0
                for (affined in update.affinedMembers) {
                    if (hasAffinedHashes) {
                        buffer.p8(affined.hash)
                    }
                    if (hasAffinedDisplayNames) {
                        buffer.pjstrnull(affined.name)
                    }
                    buffer.p1(affined.rank)
                    buffer.p4(affined.extraInfo)
                    buffer.p2(affined.joinRuneDay)
                    buffer.p1(if (affined.muted) 1 else 0)
                }
                for (banned in update.bannedMembers) {
                    if (hasAffinedHashes) {
                        buffer.p8(banned.hash)
                    }
                    if (hasAffinedDisplayNames) {
                        buffer.pjstrnull(banned.name)
                    }
                }
                buffer.p2(update.settings.size)
                for (setting in update.settings) {
                    when (setting) {
                        is ClanSettingsFull.IntClanSetting -> {
                            buffer.p4(setting.id)
                            buffer.p4(setting.value)
                        }
                        is ClanSettingsFull.LongClanSetting -> {
                            buffer.p4(setting.id or (1 shl 30))
                            buffer.p8(setting.value)
                        }
                        is ClanSettingsFull.StringClanSetting -> {
                            buffer.p4(setting.id or (2 shl 30))
                            buffer.pjstr(setting.value)
                        }
                    }
                }
            }
            ClanSettingsFull.LeaveUpdate -> {
                // No-op
            }
        }
    }
}
