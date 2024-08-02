package net.rsprot.protocol.game.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.game.incoming.codec.buttons.If1ButtonDecoder
import net.rsprot.protocol.game.incoming.codec.buttons.If3ButtonDecoder
import net.rsprot.protocol.game.incoming.codec.buttons.IfButtonDDecoder
import net.rsprot.protocol.game.incoming.codec.buttons.IfButtonTDecoder
import net.rsprot.protocol.game.incoming.codec.clan.AffinedClanSettingsAddBannedFromChannelDecoder
import net.rsprot.protocol.game.incoming.codec.clan.AffinedClanSettingsSetMutedFromChannelDecoder
import net.rsprot.protocol.game.incoming.codec.clan.ClanChannelFullRequestDecoder
import net.rsprot.protocol.game.incoming.codec.clan.ClanChannelKickUserDecoder
import net.rsprot.protocol.game.incoming.codec.clan.ClanSettingsFullRequestDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventAppletFocusDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventCameraPositionDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventKeyboardDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventMouseClickDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventMouseMoveDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventMouseScrollDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventNativeMouseClickDecoder
import net.rsprot.protocol.game.incoming.codec.events.EventNativeMouseMoveDecoder
import net.rsprot.protocol.game.incoming.codec.friendchat.FriendChatJoinLeaveDecoder
import net.rsprot.protocol.game.incoming.codec.friendchat.FriendChatKickDecoder
import net.rsprot.protocol.game.incoming.codec.friendchat.FriendChatSetRankDecoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc1Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc2Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc3Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc4Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc5Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLoc6Decoder
import net.rsprot.protocol.game.incoming.codec.locs.OpLocTDecoder
import net.rsprot.protocol.game.incoming.codec.messaging.MessagePrivateDecoder
import net.rsprot.protocol.game.incoming.codec.messaging.MessagePublicDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.ConnectionTelemetryDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.DetectModifiedClientDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.IdleDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.MapBuildCompleteDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.MembershipPromotionEligibilityDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.NoTimeoutDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.ReflectionCheckReplyDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.SendPingReplyDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.SoundJingleEndDecoder
import net.rsprot.protocol.game.incoming.codec.misc.client.WindowStatusDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.BugReportDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.ClickWorldMapDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.ClientCheatDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.CloseModalDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.HiscoreRequestDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.IfCrmViewClickDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.MoveGameClickDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.MoveMinimapClickDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.OculusLeaveDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.SendSnapshotDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.SetChatFilterSettingsDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.TeleportDecoder
import net.rsprot.protocol.game.incoming.codec.misc.user.UpdatePlayerModelDecoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc1Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc2Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc3Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc4Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc5Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpc6Decoder
import net.rsprot.protocol.game.incoming.codec.npcs.OpNpcTDecoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj1Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj2Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj3Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj4Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj5Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObj6Decoder
import net.rsprot.protocol.game.incoming.codec.objs.OpObjTDecoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer1Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer2Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer3Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer4Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer5Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer6Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer7Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayer8Decoder
import net.rsprot.protocol.game.incoming.codec.players.OpPlayerTDecoder
import net.rsprot.protocol.game.incoming.codec.resumed.ResumePCountDialogDecoder
import net.rsprot.protocol.game.incoming.codec.resumed.ResumePNameDialogDecoder
import net.rsprot.protocol.game.incoming.codec.resumed.ResumePObjDialogDecoder
import net.rsprot.protocol.game.incoming.codec.resumed.ResumePStringDialogDecoder
import net.rsprot.protocol.game.incoming.codec.resumed.ResumePauseButtonDecoder
import net.rsprot.protocol.game.incoming.codec.social.FriendListAddDecoder
import net.rsprot.protocol.game.incoming.codec.social.FriendListDelDecoder
import net.rsprot.protocol.game.incoming.codec.social.IgnoreListAddDecoder
import net.rsprot.protocol.game.incoming.codec.social.IgnoreListDelDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder

public object DesktopGameMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(If1ButtonDecoder())
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON1, 1))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON2, 2))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON3, 3))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON4, 4))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON5, 5))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON6, 6))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON7, 7))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON8, 8))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON9, 9))
                bind(If3ButtonDecoder(GameClientProt.IF_BUTTON10, 10))
                bind(IfButtonDDecoder())
                bind(IfButtonTDecoder())

                bind(OpNpc1Decoder())
                bind(OpNpc2Decoder())
                bind(OpNpc3Decoder())
                bind(OpNpc4Decoder())
                bind(OpNpc5Decoder())
                bind(OpNpc6Decoder())
                bind(OpNpcTDecoder())

                bind(OpLoc1Decoder())
                bind(OpLoc2Decoder())
                bind(OpLoc3Decoder())
                bind(OpLoc4Decoder())
                bind(OpLoc5Decoder())
                bind(OpLoc6Decoder())
                bind(OpLocTDecoder())

                bind(OpObj1Decoder())
                bind(OpObj2Decoder())
                bind(OpObj3Decoder())
                bind(OpObj4Decoder())
                bind(OpObj5Decoder())
                bind(OpObj6Decoder())
                bind(OpObjTDecoder())

                bind(OpPlayer1Decoder())
                bind(OpPlayer2Decoder())
                bind(OpPlayer3Decoder())
                bind(OpPlayer4Decoder())
                bind(OpPlayer5Decoder())
                bind(OpPlayer6Decoder())
                bind(OpPlayer7Decoder())
                bind(OpPlayer8Decoder())
                bind(OpPlayerTDecoder())

                bind(EventAppletFocusDecoder())
                bind(EventCameraPositionDecoder())
                bind(EventKeyboardDecoder())
                bind(EventMouseScrollDecoder())
                bind(EventMouseMoveDecoder())
                bind(EventNativeMouseMoveDecoder())
                bind(EventMouseClickDecoder())
                bind(EventNativeMouseClickDecoder())

                bind(ResumePauseButtonDecoder())
                bind(ResumePNameDialogDecoder())
                bind(ResumePStringDialogDecoder())
                bind(ResumePCountDialogDecoder())
                bind(ResumePObjDialogDecoder())

                bind(FriendChatKickDecoder())
                bind(FriendChatSetRankDecoder())
                bind(FriendChatJoinLeaveDecoder())

                bind(ClanChannelFullRequestDecoder())
                bind(ClanSettingsFullRequestDecoder())
                bind(ClanChannelKickUserDecoder())
                bind(AffinedClanSettingsAddBannedFromChannelDecoder())
                bind(AffinedClanSettingsSetMutedFromChannelDecoder())

                bind(FriendListAddDecoder())
                bind(FriendListDelDecoder())
                bind(IgnoreListAddDecoder())
                bind(IgnoreListDelDecoder())

                bind(MessagePublicDecoder())
                bind(MessagePrivateDecoder())

                bind(MoveGameClickDecoder())
                bind(MoveMinimapClickDecoder())
                bind(ClientCheatDecoder())
                bind(SetChatFilterSettingsDecoder())
                bind(ClickWorldMapDecoder())
                bind(OculusLeaveDecoder())
                bind(CloseModalDecoder())
                bind(TeleportDecoder())
                bind(BugReportDecoder())
                bind(SendSnapshotDecoder())
                bind(HiscoreRequestDecoder())
                bind(IfCrmViewClickDecoder())
                bind(UpdatePlayerModelDecoder())

                bind(ConnectionTelemetryDecoder())
                bind(SendPingReplyDecoder())
                bind(DetectModifiedClientDecoder())
                bind(ReflectionCheckReplyDecoder())
                bind(NoTimeoutDecoder())
                bind(IdleDecoder())
                bind(MapBuildCompleteDecoder())
                bind(MembershipPromotionEligibilityDecoder())
                bind(SoundJingleEndDecoder())
                bind(WindowStatusDecoder())
            }
        return builder.build()
    }
}
