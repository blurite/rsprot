package net.rsprot.protocol.game.outgoing.prot

import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.game.outgoing.codec.camera.CamLookAtEasedCoordEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamLookAtEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamModeEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamMoveToArc
import net.rsprot.protocol.game.outgoing.codec.camera.CamMoveToCyclesEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamMoveToEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamResetEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamRotateByEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamRotateToEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamShakeEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamSmoothResetEncoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamTargetV1Encoder
import net.rsprot.protocol.game.outgoing.codec.camera.CamTargetV2Encoder
import net.rsprot.protocol.game.outgoing.codec.camera.OculusSyncEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.ClanChannelDeltaEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.ClanChannelFullEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.ClanSettingsDeltaEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.ClanSettingsFullEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.MessageClanChannelEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.MessageClanChannelSystemEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.VarClanDisableEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.VarClanEnableEncoder
import net.rsprot.protocol.game.outgoing.codec.clan.VarClanEncoder
import net.rsprot.protocol.game.outgoing.codec.friendchat.MessageFriendChannelEncoder
import net.rsprot.protocol.game.outgoing.codec.friendchat.UpdateFriendChatChannelFullV1Encoder
import net.rsprot.protocol.game.outgoing.codec.friendchat.UpdateFriendChatChannelFullV2Encoder
import net.rsprot.protocol.game.outgoing.codec.friendchat.UpdateFriendChatChannelSingleUserEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfClearInvEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfCloseSubEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfMoveSubEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfOpenSubEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfOpenTopEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfResyncEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetAngleEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetColourEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetEventsEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetHideEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetModelEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetNpcHeadActiveEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetNpcHeadEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetObjectEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPlayerHeadEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPlayerModelBaseColourEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPlayerModelBodyTypeEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPlayerModelObjEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPlayerModelSelfEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetPositionEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetRotateSpeedEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetScrollPosEncoder
import net.rsprot.protocol.game.outgoing.codec.interfaces.IfSetTextEncoder
import net.rsprot.protocol.game.outgoing.codec.inv.UpdateInvFullEncoder
import net.rsprot.protocol.game.outgoing.codec.inv.UpdateInvPartialEncoder
import net.rsprot.protocol.game.outgoing.codec.inv.UpdateInvStopTransmitEncoder
import net.rsprot.protocol.game.outgoing.codec.logout.LogoutEncoder
import net.rsprot.protocol.game.outgoing.codec.logout.LogoutTransferEncoder
import net.rsprot.protocol.game.outgoing.codec.logout.LogoutWithReasonEncoder
import net.rsprot.protocol.game.outgoing.codec.map.RebuildNormalEncoder
import net.rsprot.protocol.game.outgoing.codec.map.RebuildRegionEncoder
import net.rsprot.protocol.game.outgoing.codec.map.RebuildWorldEntityEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.HideLocOpsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.HideNpcOpsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.HideObjOpsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.HintArrowEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.HiscoreReplyEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.MinimapToggleEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.ReflectionCheckerEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.ResetAnimsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.SendPingEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.ServerTickEndEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.SetHeatmapEnabledEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.SiteSettingsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.UpdateRebootTimerEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.UpdateUid192Encoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.UrlOpenEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.WorldEntityResetInteractionModeEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.client.WorldEntitySetInteractionModeEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.ChatFilterSettingsEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.ChatFilterSettingsPrivateChatEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.MessageGameEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.RunClientScriptEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.SetMapFlagEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.SetPlayerOpEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.TriggerOnDialogAbortEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateRunEnergyEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateRunWeightEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateStatV1Encoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateStatV2Encoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateStockMarketSlotEncoder
import net.rsprot.protocol.game.outgoing.codec.misc.player.UpdateTradingPostEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.NpcInfoLargeV5Encoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.NpcInfoSmallV5Encoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.SetNpcUpdateOriginEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.PlayerInfoEncoder
import net.rsprot.protocol.game.outgoing.codec.social.FriendListLoadedEncoder
import net.rsprot.protocol.game.outgoing.codec.social.MessagePrivateEchoEncoder
import net.rsprot.protocol.game.outgoing.codec.social.MessagePrivateEncoder
import net.rsprot.protocol.game.outgoing.codec.social.UpdateFriendListEncoder
import net.rsprot.protocol.game.outgoing.codec.social.UpdateIgnoreListEncoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiJingleEncoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiSongStopEncoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiSongV1Encoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiSongV2Encoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiSongWithSecondaryEncoder
import net.rsprot.protocol.game.outgoing.codec.sound.MidiSwapEncoder
import net.rsprot.protocol.game.outgoing.codec.sound.SynthSoundEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.LocAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.MapAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.NpcAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.NpcHeadIconSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.NpcSpotAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.PlayerAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.PlayerSpotAnimSpecificEncoder
import net.rsprot.protocol.game.outgoing.codec.specific.ProjAnimSpecificV3Encoder
import net.rsprot.protocol.game.outgoing.codec.varp.VarpLargeEncoder
import net.rsprot.protocol.game.outgoing.codec.varp.VarpResetEncoder
import net.rsprot.protocol.game.outgoing.codec.varp.VarpSmallEncoder
import net.rsprot.protocol.game.outgoing.codec.varp.VarpSyncEncoder
import net.rsprot.protocol.game.outgoing.codec.worldentity.ClearEntitiesEncoder
import net.rsprot.protocol.game.outgoing.codec.worldentity.SetActiveWorldEncoder
import net.rsprot.protocol.game.outgoing.codec.worldentity.WorldEntityInfoV3Encoder
import net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.header.UpdateZoneFullFollowsEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.header.UpdateZonePartialFollowsEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocAddChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocDelEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocMergeEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.MapAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.MapProjAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjAddEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjCountEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjCustomiseEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjDelEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjEnabledOpsEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjUncustomiseEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.SoundAreaEncoder
import net.rsprot.protocol.game.outgoing.map.RebuildLogin
import net.rsprot.protocol.game.outgoing.map.RebuildNormal
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

public object DesktopGameMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(huffmanCodecProvider: HuffmanCodecProvider): MessageEncoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(IfResyncEncoder())
                bind(IfOpenTopEncoder())
                bind(IfOpenSubEncoder())
                bind(IfCloseSubEncoder())
                bind(IfMoveSubEncoder())
                bind(IfClearInvEncoder())
                bind(IfSetEventsEncoder())
                bind(IfSetPositionEncoder())
                bind(IfSetScrollPosEncoder())
                bind(IfSetRotateSpeedEncoder())
                bind(IfSetTextEncoder())
                bind(IfSetHideEncoder())
                bind(IfSetAngleEncoder())
                bind(IfSetObjectEncoder())
                bind(IfSetColourEncoder())
                bind(IfSetAnimEncoder())
                bind(IfSetNpcHeadEncoder())
                bind(IfSetNpcHeadActiveEncoder())
                bind(IfSetPlayerHeadEncoder())
                bind(IfSetModelEncoder())
                bind(IfSetPlayerModelBaseColourEncoder())
                bind(IfSetPlayerModelBodyTypeEncoder())
                bind(IfSetPlayerModelObjEncoder())
                bind(IfSetPlayerModelSelfEncoder())

                bind(MidiSongV2Encoder())
                bind(MidiSongWithSecondaryEncoder())
                bind(MidiSwapEncoder())
                bind(MidiSongStopEncoder())
                bind(MidiSongV1Encoder())
                bind(MidiJingleEncoder())
                bind(SynthSoundEncoder())

                bind(UpdateZoneFullFollowsEncoder())
                bind(UpdateZonePartialFollowsEncoder())
                bind(DesktopUpdateZonePartialEnclosedEncoder())

                bind(LocAddChangeEncoder())
                bind(LocDelEncoder())
                bind(LocAnimEncoder())
                bind(LocMergeEncoder())
                bind(ObjAddEncoder())
                bind(ObjDelEncoder())
                bind(ObjCountEncoder())
                bind(ObjCustomiseEncoder())
                bind(ObjUncustomiseEncoder())
                bind(ObjEnabledOpsEncoder())
                bind(MapAnimEncoder())
                bind(MapProjAnimEncoder())
                bind(SoundAreaEncoder())

                bind(ProjAnimSpecificV3Encoder())
                bind(MapAnimSpecificEncoder())
                bind(LocAnimSpecificEncoder())
                bind(NpcHeadIconSpecificEncoder())
                bind(NpcSpotAnimSpecificEncoder())
                bind(NpcAnimSpecificEncoder())
                bind(PlayerAnimSpecificEncoder())
                bind(PlayerSpotAnimSpecificEncoder())

                bind(PlayerInfoEncoder())
                bind(NpcInfoSmallV5Encoder())
                bind(NpcInfoLargeV5Encoder())
                bind(SetNpcUpdateOriginEncoder())

                bind(ClearEntitiesEncoder())
                bind(SetActiveWorldEncoder())
                bind(WorldEntityInfoV3Encoder())

                bindWithAlts(RebuildNormalEncoder(), RebuildLogin::class.java, RebuildNormal::class.java)
                bind(RebuildRegionEncoder())
                bind(RebuildWorldEntityEncoder())

                bind(VarpSmallEncoder())
                bind(VarpLargeEncoder())
                bind(VarpResetEncoder())
                bind(VarpSyncEncoder())

                bind(CamShakeEncoder())
                bind(CamResetEncoder())
                bind(CamSmoothResetEncoder())
                bind(CamMoveToEncoder())
                bind(CamMoveToCyclesEncoder())
                bind(CamMoveToArc())
                bind(CamLookAtEncoder())
                bind(CamLookAtEasedCoordEncoder())
                bind(CamRotateByEncoder())
                bind(CamRotateToEncoder())
                bind(CamModeEncoder())
                bind(CamTargetV2Encoder())
                bind(CamTargetV1Encoder())
                bind(OculusSyncEncoder())

                bind(UpdateInvFullEncoder())
                bind(UpdateInvPartialEncoder())
                bind(UpdateInvStopTransmitEncoder())

                bind(MessagePrivateEncoder(huffmanCodecProvider))
                bind(MessagePrivateEchoEncoder(huffmanCodecProvider))
                bind(FriendListLoadedEncoder())
                bind(UpdateFriendListEncoder())
                bind(UpdateIgnoreListEncoder())

                bind(UpdateFriendChatChannelFullV1Encoder())
                bind(UpdateFriendChatChannelFullV2Encoder())
                bind(UpdateFriendChatChannelSingleUserEncoder())
                bind(MessageFriendChannelEncoder(huffmanCodecProvider))

                bind(VarClanEncoder())
                bind(VarClanEnableEncoder())
                bind(VarClanDisableEncoder())
                bind(ClanChannelFullEncoder())
                bind(ClanChannelDeltaEncoder())
                bind(ClanSettingsFullEncoder())
                bind(ClanSettingsDeltaEncoder())
                bind(MessageClanChannelEncoder(huffmanCodecProvider))
                bind(MessageClanChannelSystemEncoder(huffmanCodecProvider))

                bind(LogoutEncoder())
                bind(LogoutWithReasonEncoder())
                bind(LogoutTransferEncoder())

                bind(UpdateRunWeightEncoder())
                bind(UpdateRunEnergyEncoder())
                bind(SetMapFlagEncoder())
                bind(SetPlayerOpEncoder())
                bind(UpdateStatV2Encoder())
                bind(UpdateStatV1Encoder())

                bind(RunClientScriptEncoder())
                bind(TriggerOnDialogAbortEncoder())
                bind(MessageGameEncoder())
                bind(ChatFilterSettingsEncoder())
                bind(ChatFilterSettingsPrivateChatEncoder())
                bind(UpdateTradingPostEncoder())
                bind(UpdateStockMarketSlotEncoder())

                bind(HintArrowEncoder())
                bind(ResetAnimsEncoder())
                bind(UpdateRebootTimerEncoder())
                bind(SetHeatmapEnabledEncoder())
                bind(MinimapToggleEncoder())
                bind(ServerTickEndEncoder())
                bind(HideNpcOpsEncoder())
                bind(HideObjOpsEncoder())
                bind(HideLocOpsEncoder())
                bind(WorldEntitySetInteractionModeEncoder())
                bind(WorldEntityResetInteractionModeEncoder())

                bind(UrlOpenEncoder())
                bind(SiteSettingsEncoder())
                bind(UpdateUid192Encoder())
                bind(ReflectionCheckerEncoder())
                bind(SendPingEncoder())
                bind(HiscoreReplyEncoder())
            }
        return builder.build()
    }
}
