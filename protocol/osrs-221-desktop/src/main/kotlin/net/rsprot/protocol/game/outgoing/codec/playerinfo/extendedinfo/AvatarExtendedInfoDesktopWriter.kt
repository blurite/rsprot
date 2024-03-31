package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.outgoing.info.playerinfo.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfoBlocks
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder.PlayerExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType

public class AvatarExtendedInfoDesktopWriter : AvatarExtendedInfoWriter(
    PlatformType.DESKTOP,
    PlayerExtendedInfoEncoders(
        PlatformType.DESKTOP,
        AppearanceEncoder(),
        ChatEncoder(),
        ExactMoveEncoder(),
        FaceAngleEncoder(),
        FacePathingEntityEncoder(),
        HitEncoder(),
        MoveSpeedEncoder(),
        SayEncoder(),
        SequenceEncoder(),
        SpotAnimEncoder(),
        TemporaryMoveSpeedEncoder(),
        TintingEncoder(),
    ),
) {
    private fun convertFlags(constantFlags: Int): Int {
        var platformFlags = 0
        if (constantFlags and PlayerAvatarExtendedInfo.APPEARANCE != 0) {
            platformFlags = platformFlags or APPEARANCE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.MOVE_SPEED != 0) {
            platformFlags = platformFlags or MOVE_SPEED
        }
        if (constantFlags and PlayerAvatarExtendedInfo.FACE_PATHINGENTITY != 0) {
            platformFlags = platformFlags or FACE_PATHINGENTITY
        }
        if (constantFlags and PlayerAvatarExtendedInfo.TINTING != 0) {
            platformFlags = platformFlags or TINTING
        }
        if (constantFlags and PlayerAvatarExtendedInfo.FACE_ANGLE != 0) {
            platformFlags = platformFlags or FACE_ANGLE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SAY != 0) {
            platformFlags = platformFlags or SAY
        }
        if (constantFlags and PlayerAvatarExtendedInfo.HITS != 0) {
            platformFlags = platformFlags or HITS
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SEQUENCE != 0) {
            platformFlags = platformFlags or SEQUENCE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.CHAT != 0) {
            platformFlags = platformFlags or CHAT
        }
        if (constantFlags and PlayerAvatarExtendedInfo.TEMP_MOVE_SPEED != 0) {
            platformFlags = platformFlags or TEMP_MOVE_SPEED
        }
        if (constantFlags and PlayerAvatarExtendedInfo.EXACT_MOVE != 0) {
            platformFlags = platformFlags or EXACT_MOVE
        }
        if (constantFlags and PlayerAvatarExtendedInfo.SPOTANIM != 0) {
            platformFlags = platformFlags or SPOTANIM
        }
        return platformFlags
    }

    override fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: PlayerAvatarExtendedInfoBlocks,
    ) {
        var platformFlag = convertFlags(flag)
        if (platformFlag and 0xFF.inv() != 0) platformFlag = platformFlag or EXTENDED_SHORT
        if (platformFlag and 0xFFFF.inv() != 0) platformFlag = platformFlag or EXTENDED_MEDIUM
        buffer.p1(platformFlag)
        if (platformFlag and EXTENDED_SHORT != 0) {
            buffer.p1(platformFlag shr 8)
        }
        if (platformFlag and EXTENDED_MEDIUM != 0) {
            buffer.p1(platformFlag shr 16)
        }

        if (platformFlag and FACE_ANGLE != 0) {
            pCachedData(buffer, blocks.faceAngle)
        }
        // Old chat
        if (platformFlag and SEQUENCE != 0) {
            pCachedData(buffer, blocks.sequence)
        }
        if (platformFlag and HITS != 0) {
            pOnDemandData(buffer, localIndex, blocks.hit, observerIndex)
        }
        if (platformFlag and EXACT_MOVE != 0) {
            pCachedData(buffer, blocks.exactMove)
        }
        if (platformFlag and CHAT != 0) {
            pCachedData(buffer, blocks.chat)
        }
        if (platformFlag and TEMP_MOVE_SPEED != 0) {
            pCachedData(buffer, blocks.temporaryMoveSpeed)
        }
        // name extras
        if (platformFlag and SAY != 0) {
            pCachedData(buffer, blocks.say)
        }
        if (platformFlag and TINTING != 0) {
            pOnDemandData(buffer, localIndex, blocks.tinting, observerIndex)
        }
        if (platformFlag and MOVE_SPEED != 0) {
            pCachedData(buffer, blocks.moveSpeed)
        }
        if (platformFlag and APPEARANCE != 0) {
            pCachedData(buffer, blocks.appearance)
        }
        if (platformFlag and FACE_PATHINGENTITY != 0) {
            pCachedData(buffer, blocks.facePathingEntity)
        }
        if (platformFlag and SPOTANIM != 0) {
            pCachedData(buffer, blocks.spotAnims)
        }
    }

    @Suppress("unused")
    private companion object {
        private const val EXTENDED_SHORT = 0x1
        private const val FACE_ANGLE = 0x2
        private const val APPEARANCE = 0x4
        private const val SAY = 0x8
        private const val CHAT_OLD = 0x10
        private const val HITS = 0x20
        private const val FACE_PATHINGENTITY = 0x40
        private const val SEQUENCE = 0x80
        private const val MOVE_SPEED = 0x200
        private const val CHAT = 0x400
        private const val EXTENDED_MEDIUM = 0x800
        private const val TEMP_MOVE_SPEED = 0x1000
        private const val TINTING = 0x2000
        private const val EXACT_MOVE = 0x4000
        private const val SPOTANIM = 0x10000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        private const val NAME_EXTRAS = 0x100
    }
}
