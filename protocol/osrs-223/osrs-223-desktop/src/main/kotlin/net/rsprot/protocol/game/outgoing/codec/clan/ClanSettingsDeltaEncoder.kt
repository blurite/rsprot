package net.rsprot.protocol.game.outgoing.codec.clan

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.ClanSettingsDelta
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClanSettingsDeltaEncoder : MessageEncoder<ClanSettingsDelta> {
    override val prot: ServerProt = GameServerProt.CLANSETTINGS_DELTA

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: ClanSettingsDelta,
    ) {
        buffer.p1(message.clanType)
        buffer.p8(message.owner)
        buffer.p4(message.updateNum)
        val updates = message.updates
        for (update in updates) {
            when (update) {
                is ClanSettingsDelta.SetClanOwnerUpdate -> {
                    buffer.p1(15)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.AddBannedUpdate -> {
                    buffer.p1(3)
                    val hash = update.hash
                    if (hash and 0xFF != 0xFF.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0xFF)
                    }
                    buffer.pjstrnull(update.name)
                }
                is ClanSettingsDelta.AddMemberV1Update -> {
                    buffer.p1(1)
                    val hash = update.hash
                    if (hash and 0xFF != 0xFF.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0xFF)
                    }
                    buffer.pjstrnull(update.name)
                }
                is ClanSettingsDelta.AddMemberV2Update -> {
                    buffer.p1(13)
                    val hash = update.hash
                    if (hash and 0xFF != 0xFF.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0xFF)
                    }
                    buffer.pjstrnull(update.name)
                    buffer.p2(update.joinRuneDay)
                }
                is ClanSettingsDelta.BaseSettingsUpdate -> {
                    buffer.p1(4)
                    buffer.p1(if (update.allowUnaffined) 1 else 0)
                    buffer.p1(update.talkRank)
                    buffer.p1(update.kickRank)
                    buffer.p1(update.lootshareRank)
                    buffer.p1(update.coinshareRank)
                }
                is ClanSettingsDelta.DeleteBannedUpdate -> {
                    buffer.p1(6)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.DeleteMemberUpdate -> {
                    buffer.p1(5)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.SetClanNameUpdate -> {
                    buffer.p1(12)
                    buffer.pjstr(update.clanName)

                    // Unused in all clients, including RS3
                    buffer.p4(0)
                }
                is ClanSettingsDelta.SetIntSettingUpdate -> {
                    buffer.p1(8)
                    buffer.p4(update.setting)
                    buffer.p4(update.value)
                }
                is ClanSettingsDelta.SetLongSettingUpdate -> {
                    buffer.p1(9)
                    buffer.p4(update.setting)
                    buffer.p8(update.value)
                }
                is ClanSettingsDelta.SetMemberExtraInfoUpdate -> {
                    buffer.p1(7)
                    buffer.p2(update.index)
                    buffer.p4(update.value)
                    buffer.p1(update.startBit)
                    buffer.p1(update.endBit)
                }
                is ClanSettingsDelta.SetMemberMutedUpdate -> {
                    buffer.p1(14)
                    buffer.p2(update.index)
                    buffer.p1(if (update.muted) 1 else 0)
                }
                is ClanSettingsDelta.SetMemberRankUpdate -> {
                    buffer.p1(2)
                    buffer.p2(update.index)
                    buffer.p1(update.rank)
                }
                is ClanSettingsDelta.SetStringSettingUpdate -> {
                    buffer.p1(10)
                    buffer.p4(update.setting)
                    buffer.pjstr(update.value)
                }
                is ClanSettingsDelta.SetVarbitSettingUpdate -> {
                    buffer.p1(11)
                    buffer.p4(update.setting)
                    buffer.p4(update.value)
                    buffer.p1(update.startBit)
                    buffer.p1(update.endBit)
                }
            }
        }
        buffer.p1(0)
    }
}
