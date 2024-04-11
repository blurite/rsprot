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
                is ClanSettingsDelta.ClanSettingDeltaSetClanOwnerUpdate -> {
                    buffer.p1(15)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddBannedUpdate -> {
                    buffer.p1(3)
                    val hash = update.hash
                    if (hash and 0xFF != 255.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0)
                    }
                    buffer.pjstrnull(update.name)
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddMemberV1Update -> {
                    buffer.p1(1)
                    val hash = update.hash
                    if (hash and 0xFF != 255.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0)
                    }
                    buffer.pjstrnull(update.name)
                }
                is ClanSettingsDelta.ClanSettingsDeltaAddMemberV2Update -> {
                    buffer.p1(13)
                    val hash = update.hash
                    if (hash and 0xFF != 255.toLong()) {
                        buffer.p8(hash)
                    } else {
                        buffer.p1(0)
                    }
                    buffer.pjstrnull(update.name)
                    buffer.p2(update.joinRuneDay)
                }
                is ClanSettingsDelta.ClanSettingsDeltaBaseSettingsUpdate -> {
                    buffer.p1(4)
                    buffer.p1(if (update.allowUnaffined) 1 else 0)
                    buffer.p1(update.talkRank)
                    buffer.p1(update.kickRank)
                    buffer.p1(update.lootshareRank)
                    buffer.p1(update.coinshareRank)
                }
                is ClanSettingsDelta.ClanSettingsDeltaDeleteBannedUpdate -> {
                    buffer.p1(6)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.ClanSettingsDeltaDeleteMemberUpdate -> {
                    buffer.p1(5)
                    buffer.p2(update.index)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetClanNameUpdate -> {
                    buffer.p1(12)
                    buffer.pjstr(update.clanName)

                    // Unused in all clients, including RS3
                    buffer.p4(0)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetIntSettingUpdate -> {
                    buffer.p1(8)
                    buffer.p4(update.setting)
                    buffer.p4(update.value)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetLongSettingUpdate -> {
                    buffer.p1(9)
                    buffer.p4(update.setting)
                    buffer.p8(update.value)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberExtraInfoUpdate -> {
                    buffer.p1(7)
                    buffer.p2(update.index)
                    buffer.p4(update.value)
                    buffer.p1(update.startBit)
                    buffer.p1(update.endBit)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberMutedUpdate -> {
                    buffer.p1(14)
                    buffer.p2(update.index)
                    buffer.p1(if (update.muted) 1 else 0)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetMemberRankUpdate -> {
                    buffer.p1(2)
                    buffer.p2(update.index)
                    buffer.p1(update.rank)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetStringSettingUpdate -> {
                    buffer.p1(10)
                    buffer.p4(update.setting)
                    buffer.pjstr(update.value)
                }
                is ClanSettingsDelta.ClanSettingsDeltaSetVarbitSettingUpdate -> {
                    buffer.p1(11)
                    buffer.p4(update.setting)
                    buffer.p4(update.value)
                    buffer.p1(update.startBit)
                    buffer.p1(update.endBit)
                }
            }
        }
    }
}
