package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerAppearanceEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerChatEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerExactMoveEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerFaceAngleEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerFacePathingEntityEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerHitEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerMoveSpeedEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerSayEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerSequenceEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerSpotAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerTemporaryMoveSpeedEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.PlayerTintingEncoder
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfoBlocks
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder.PlayerExtendedInfoEncoders

public class PlayerAvatarExtendedInfoDesktopWriter :
    AvatarExtendedInfoWriter<PlayerExtendedInfoEncoders, PlayerAvatarExtendedInfoBlocks>(
        OldSchoolClientType.DESKTOP,
        PlayerExtendedInfoEncoders(
            OldSchoolClientType.DESKTOP,
            PlayerAppearanceEncoder(),
            PlayerChatEncoder(),
            PlayerExactMoveEncoder(),
            PlayerFaceAngleEncoder(),
            PlayerFacePathingEntityEncoder(),
            PlayerHitEncoder(),
            PlayerMoveSpeedEncoder(),
            PlayerSayEncoder(),
            PlayerSequenceEncoder(),
            PlayerSpotAnimEncoder(),
            PlayerTemporaryMoveSpeedEncoder(),
            PlayerTintingEncoder(),
        ),
    ) {
    private fun convertFlags(constantFlags: Int): Int {
        var clientFlags = 0
        if (constantFlags and PlayerAvatarExtendedInfo.APPEARANCE != 0) {
            clientFlags = clientFlags or APPEARANCE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.MOVE_SPEED != 0) {
            clientFlags = clientFlags or MOVE_SPEED
        }
        if (constantFlags and PlayerAvatarExtendedInfo.FACE_PATHINGENTITY != 0) {
            clientFlags = clientFlags or FACE_PATHINGENTITY
        }
        if (constantFlags and PlayerAvatarExtendedInfo.TINTING != 0) {
            clientFlags = clientFlags or TINTING
        }
        if (constantFlags and PlayerAvatarExtendedInfo.FACE_ANGLE != 0) {
            clientFlags = clientFlags or FACE_ANGLE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SAY != 0) {
            clientFlags = clientFlags or SAY
        }
        if (constantFlags and PlayerAvatarExtendedInfo.HITS != 0) {
            clientFlags = clientFlags or HITS
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SEQUENCE != 0) {
            clientFlags = clientFlags or SEQUENCE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.CHAT != 0) {
            clientFlags = clientFlags or CHAT
        }
        if (constantFlags and PlayerAvatarExtendedInfo.TEMP_MOVE_SPEED != 0) {
            clientFlags = clientFlags or TEMP_MOVE_SPEED
        }
        if (constantFlags and PlayerAvatarExtendedInfo.EXACT_MOVE != 0) {
            clientFlags = clientFlags or EXACT_MOVE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SPOTANIM != 0) {
            clientFlags = clientFlags or SPOTANIM
        }
        return clientFlags
    }

    override fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: PlayerAvatarExtendedInfoBlocks,
    ) {
        var clientFlag = convertFlags(flag)
        if (clientFlag and 0xFF.inv() != 0) clientFlag = clientFlag or EXTENDED_SHORT
        if (clientFlag and 0xFFFF.inv() != 0) clientFlag = clientFlag or EXTENDED_MEDIUM
        buffer.p1(clientFlag)
        if (clientFlag and EXTENDED_SHORT != 0) {
            buffer.p1(clientFlag shr 8)
        }
        if (clientFlag and EXTENDED_MEDIUM != 0) {
            buffer.p1(clientFlag shr 16)
        }

        // Name extras
        if (clientFlag and FACE_PATHINGENTITY != 0) {
            pCachedData(buffer, blocks.facePathingEntity)
        }
        if (clientFlag and MOVE_SPEED != 0) {
            pCachedData(buffer, blocks.moveSpeed)
        }
        if (clientFlag and APPEARANCE != 0) {
            pCachedData(buffer, blocks.appearance)
        }
        if (clientFlag and HITS != 0) {
            pOnDemandData(buffer, localIndex, blocks.hit, observerIndex)
        }
        // Old chat
        if (clientFlag and CHAT != 0) {
            pCachedData(buffer, blocks.chat)
        }
        if (clientFlag and TEMP_MOVE_SPEED != 0) {
            pCachedData(buffer, blocks.temporaryMoveSpeed)
        }
        if (clientFlag and TINTING != 0) {
            pOnDemandData(buffer, localIndex, blocks.tinting, observerIndex)
        }
        if (clientFlag and SAY != 0) {
            pCachedData(buffer, blocks.say)
        }
        if (clientFlag and SEQUENCE != 0) {
            pCachedData(buffer, blocks.sequence)
        }
        if (clientFlag and SPOTANIM != 0) {
            pCachedData(buffer, blocks.spotAnims)
        }
        if (clientFlag and FACE_ANGLE != 0) {
            pCachedData(buffer, blocks.faceAngle)
        }
        if (clientFlag and EXACT_MOVE != 0) {
            pCachedData(buffer, blocks.exactMove)
        }
    }

    @Suppress("unused")
    private companion object {
        private const val EXTENDED_SHORT = 0x4
        private const val EXTENDED_MEDIUM = 0x100

        private const val SEQUENCE = 0x1
        private const val HITS = 0x2
        private const val FACE_PATHINGENTITY = 0x8
        private const val APPEARANCE = 0x10
        private const val CHAT_OLD = 0x20
        private const val FACE_ANGLE = 0x40
        private const val SAY = 0x80
        private const val MOVE_SPEED = 0x200
        private const val TEMP_MOVE_SPEED = 0x800
        private const val TINTING = 0x1000
        private const val EXACT_MOVE = 0x4000
        private const val CHAT = 0x8000
        private const val SPOTANIM = 0x10000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        private const val NAME_EXTRAS = 0x400
    }
}
