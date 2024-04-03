package net.rsprot.protocol.game.incoming.prot

import net.rsprot.protocol.ClientProt

public enum class GameClientProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    // If buttons
    IF_BUTTON(GameClientProtId.IF_BUTTON, 4),
    IF_BUTTON1(GameClientProtId.IF_BUTTON1, 8),
    IF_BUTTON2(GameClientProtId.IF_BUTTON2, 8),
    IF_BUTTON3(GameClientProtId.IF_BUTTON3, 8),
    IF_BUTTON4(GameClientProtId.IF_BUTTON4, 8),
    IF_BUTTON5(GameClientProtId.IF_BUTTON5, 8),
    IF_BUTTON6(GameClientProtId.IF_BUTTON6, 8),
    IF_BUTTON7(GameClientProtId.IF_BUTTON7, 8),
    IF_BUTTON8(GameClientProtId.IF_BUTTON8, 8),
    IF_BUTTON9(GameClientProtId.IF_BUTTON9, 8),
    IF_BUTTON10(GameClientProtId.IF_BUTTON10, 8),
    IF_BUTTOND(GameClientProtId.IF_BUTTOND, 16),
    IF_BUTTONT(GameClientProtId.IF_BUTTONT, 16),

    // Op npc
    OPNPC1(GameClientProtId.OPNPC1, 3),
    OPNPC2(GameClientProtId.OPNPC2, 3),
    OPNPC3(GameClientProtId.OPNPC3, 3),
    OPNPC4(GameClientProtId.OPNPC4, 3),
    OPNPC5(GameClientProtId.OPNPC5, 3),
    OPNPC6(GameClientProtId.OPNPC6, 2),
    OPNPCU(GameClientProtId.OPNPCU, 11),
    OPNPCT(GameClientProtId.OPNPCT, 11),

    // Op loc
    OPLOC1(GameClientProtId.OPLOC1, 7),
    OPLOC2(GameClientProtId.OPLOC2, 7),
    OPLOC3(GameClientProtId.OPLOC3, 7),
    OPLOC4(GameClientProtId.OPLOC4, 7),
    OPLOC5(GameClientProtId.OPLOC5, 7),
    OPLOC6(GameClientProtId.OPLOC6, 2),
    OPLOCU(GameClientProtId.OPLOCU, 15),
    OPLOCT(GameClientProtId.OPLOCT, 15),

    // Op obj
    OPOBJ1(GameClientProtId.OPOBJ1, 7),
    OPOBJ2(GameClientProtId.OPOBJ2, 7),
    OPOBJ3(GameClientProtId.OPOBJ3, 7),
    OPOBJ4(GameClientProtId.OPOBJ4, 7),
    OPOBJ5(GameClientProtId.OPOBJ5, 7),
    OPOBJ6(GameClientProtId.OPOBJ6, 6),
    OPOBJU(GameClientProtId.OPOBJU, 15),
    OPOBJT(GameClientProtId.OPOBJT, 15),

    // Op player
    OPPLAYER1(GameClientProtId.OPPLAYER1, 3),
    OPPLAYER2(GameClientProtId.OPPLAYER2, 3),
    OPPLAYER3(GameClientProtId.OPPLAYER3, 3),
    OPPLAYER4(GameClientProtId.OPPLAYER4, 3),
    OPPLAYER5(GameClientProtId.OPPLAYER5, 3),
    OPPLAYER6(GameClientProtId.OPPLAYER6, 3),
    OPPLAYER7(GameClientProtId.OPPLAYER7, 3),
    OPPLAYER8(GameClientProtId.OPPLAYER8, 3),
    OPPLAYERU(GameClientProtId.OPPLAYERU, 11),
    OPPLAYERT(GameClientProtId.OPPLAYERT, 11),

    // Op held
    OPHELD6(GameClientProtId.OPHELD6, 2),

    // Events
    EVENT_APPLET_FOCUS(GameClientProtId.EVENT_APPLET_FOCUS, 1),
    EVENT_CAMERA_POSITION(GameClientProtId.EVENT_CAMERA_POSITION, 4),
    EVENT_KEYBOARD(GameClientProtId.EVENT_KEYBOARD, -2),
    EVENT_MOUSE_SCROLL(GameClientProtId.EVENT_MOUSE_SCROLL, 2),
    EVENT_MOUSE_MOVE(GameClientProtId.EVENT_MOUSE_MOVE, -1),
    EVENT_NATIVE_MOUSE_MOVE(GameClientProtId.EVENT_NATIVE_MOUSE_MOVE, -1),
    EVENT_MOUSE_CLICK(GameClientProtId.EVENT_MOUSE_CLICK, 6),
    EVENT_NATIVE_MOUSE_CLICK(GameClientProtId.EVENT_NATIVE_MOUSE_CLICK, 7),

    // Resume events
    RESUME_PAUSEBUTTON(GameClientProtId.RESUME_PAUSEBUTTON, 6),
    RESUME_P_NAMEDIALOG(GameClientProtId.RESUME_P_NAMEDIALOG, -1),
    RESUME_P_STRINGDIALOG(GameClientProtId.RESUME_P_STRINGDIALOG, -1),
    RESUME_P_COUNTDIALOG(GameClientProtId.RESUME_P_COUNTDIALOG, 4),
    RESUME_P_OBJDIALOG(GameClientProtId.RESUME_P_OBJDIALOG, 2),

    // Friend chat packets
    FRIENDCHAT_KICK(GameClientProtId.FRIENDCHAT_KICK, -1),
    FRIENDCHAT_SETRANK(GameClientProtId.FRIENDCHAT_SETRANK, -1),
    FRIENDCHAT_JOIN_LEAVE(GameClientProtId.FRIENDCHAT_JOIN_LEAVE, -1),

    // Clan packets
    CLANCHANNEL_FULL_REQUEST(GameClientProtId.CLANCHANNEL_FULL_REQUEST, 1),
    CLANSETTINGS_FULL_REQUEST(GameClientProtId.CLANSETTINGS_FULL_REQUEST, 1),
    CLANCHANNEL_KICKUSER(GameClientProtId.CLANCHANNEL_KICKUSER, -1),
    AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL(GameClientProtId.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL, -1),
    AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL(GameClientProtId.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL, -1),

    // Socials
    FRIENDLIST_ADD(GameClientProtId.FRIENDLIST_ADD, -1),
    FRIENDLIST_DEL(GameClientProtId.FRIENDLIST_DEL, -1),
    IGNORELIST_ADD(GameClientProtId.IGNORELIST_ADD, -1),
    IGNORELIST_DEL(GameClientProtId.IGNORELIST_DEL, -1),

    // Messaging
    MESSAGE_PUBLIC(GameClientProtId.MESSAGE_PUBLIC, -1),
    MESSAGE_PRIVATE(GameClientProtId.MESSAGE_PRIVATE, -2),

    // Misc. user packets
    MOVE_GAMECLICK(GameClientProtId.MOVE_GAMECLICK, -1),
    MOVE_MINIMAPCLICK(GameClientProtId.MOVE_MINIMAPCLICK, -1),
    CLIENT_CHEAT(GameClientProtId.CLIENT_CHEAT, -1),
    SET_CHATFILTERSETTINGS(GameClientProtId.SET_CHATFILTERSETTINGS, 3),
    CLICKWORLDMAP(GameClientProtId.CLICKWORLDMAP, 4),
    OCULUS_LEAVE(GameClientProtId.OCULUS_LEAVE, 0),
    CLOSE_MODAL(GameClientProtId.CLOSE_MODAL, 0),
    TELEPORT(GameClientProtId.TELEPORT, 9),
    BUG_REPORT(GameClientProtId.BUG_REPORT, -2),
    SEND_SNAPSHOT(GameClientProtId.SEND_SNAPSHOT, -1),
    HISCORE_REQUEST(GameClientProtId.HISCORE_REQUEST, -1),
    IF_CRMVIEW_CLICK(GameClientProtId.IF_CRMVIEW_CLICK, 22),
    UPDATE_PLAYER_MODEL(GameClientProtId.UPDATE_PLAYER_MODEL, 13),

    // Misc. client packets
    TIMINGS(GameClientProtId.TIMINGS, -1),
    SEND_PING_REPLY(GameClientProtId.SEND_PING_REPLY, 10),
    DETECT_MODIFIED_CLIENT(GameClientProtId.DETECT_MODIFIED_CLIENT, 4),
    REFLECTION_CHECK_REPLY(GameClientProtId.REFLECTION_CHECK_REPLY, -1),
    NO_TIMEOUT(GameClientProtId.NO_TIMEOUT, 0),
    IDLE(GameClientProtId.IDLE, 0),
    MAP_BUILD_COMPLETE(GameClientProtId.MAP_BUILD_COMPLETE, 0),
    MEMBERSHIP_PROMOTION_ELIGIBILITY(GameClientProtId.MEMBERSHIP_PROMOTION_ELIGIBILITY, 2),
    SOUND_JINGLEEND(GameClientProtId.SOUND_JINGLEEND, 4),
    WINDOW_STATUS(GameClientProtId.WINDOW_STATUS, 5),
}
