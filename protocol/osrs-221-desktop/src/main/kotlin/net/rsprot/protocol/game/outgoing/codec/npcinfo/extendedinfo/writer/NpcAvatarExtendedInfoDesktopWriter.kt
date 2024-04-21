package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcBaseAnimationSetEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcBodyCustomisationEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcCombatLevelChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcExactMoveEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcFaceCoordEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcFacePathingEntityEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcHeadCustomisationEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcHeadIconCustomisationEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcHitEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcNameChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcSayEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcSequenceEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcSpotAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcTintingEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcTransformationEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.NpcVisibleOpsEncoder
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExtendedInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExtendedInfoBlocks
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType

@Suppress("DuplicatedCode")
public class NpcAvatarExtendedInfoDesktopWriter :
    AvatarExtendedInfoWriter<NpcExtendedInfoEncoders, NpcAvatarExtendedInfoBlocks>(
        PlatformType.DESKTOP,
        NpcExtendedInfoEncoders(
            PlatformType.DESKTOP,
            NpcSpotAnimEncoder(),
            NpcSayEncoder(),
            NpcVisibleOpsEncoder(),
            NpcExactMoveEncoder(),
            NpcSequenceEncoder(),
            NpcTintingEncoder(),
            NpcHeadIconCustomisationEncoder(),
            NpcNameChangeEncoder(),
            NpcHeadCustomisationEncoder(),
            NpcBodyCustomisationEncoder(),
            NpcTransformationEncoder(),
            NpcCombatLevelChangeEncoder(),
            NpcHitEncoder(),
            NpcFaceCoordEncoder(),
            NpcFacePathingEntityEncoder(),
            NpcBaseAnimationSetEncoder(),
        ),
    ) {
    private fun convertFlags(constantFlags: Int): Int {
        var platformFlags = 0
        if (constantFlags and NpcAvatarExtendedInfo.FACE_PATHINGENTITY != 0) {
            platformFlags = platformFlags or FACE_PATHINGENTITY
        }
        if (constantFlags and NpcAvatarExtendedInfo.TINTING != 0) {
            platformFlags = platformFlags or TINTING
        }
        if (constantFlags and NpcAvatarExtendedInfo.SAY != 0) {
            platformFlags = platformFlags or SAY
        }
        if (constantFlags and NpcAvatarExtendedInfo.HITS != 0) {
            platformFlags = platformFlags or HITS
        }
        if (constantFlags and NpcAvatarExtendedInfo.SEQUENCE != 0) {
            platformFlags = platformFlags or SEQUENCE
        }
        if (constantFlags and NpcAvatarExtendedInfo.EXACT_MOVE != 0) {
            platformFlags = platformFlags or EXACT_MOVE
        }
        if (constantFlags and NpcAvatarExtendedInfo.SPOTANIM != 0) {
            platformFlags = platformFlags or SPOTANIM
        }
        if (constantFlags and NpcAvatarExtendedInfo.FACE_COORD != 0) {
            platformFlags = platformFlags or FACE_COORD
        }
        if (constantFlags and NpcAvatarExtendedInfo.TRANSFORMATION != 0) {
            platformFlags = platformFlags or TRANSFORMATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.BODY_CUSTOMISATION != 0) {
            platformFlags = platformFlags or BODY_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.HEAD_CUSTOMISATION != 0) {
            platformFlags = platformFlags or HEAD_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.LEVEL_CHANGE != 0) {
            platformFlags = platformFlags or LEVEL_CHANGE
        }
        if (constantFlags and NpcAvatarExtendedInfo.OPS != 0) {
            platformFlags = platformFlags or OPS
        }
        if (constantFlags and NpcAvatarExtendedInfo.NAME_CHANGE != 0) {
            platformFlags = platformFlags or NAME_CHANGE
        }
        if (constantFlags and NpcAvatarExtendedInfo.HEADICON_CUSTOMISATION != 0) {
            platformFlags = platformFlags or HEADICON_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.BAS_CHANGE != 0) {
            platformFlags = platformFlags or BAS_CHANGE
        }
        return platformFlags
    }

    override fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: NpcAvatarExtendedInfoBlocks,
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

        // old spotanim
        if (platformFlag and SPOTANIM != 0) {
            pCachedData(buffer, blocks.spotAnims)
        }
        if (platformFlag and SAY != 0) {
            pCachedData(buffer, blocks.say)
        }
        if (platformFlag and OPS != 0) {
            pCachedData(buffer, blocks.visibleOps)
        }
        if (platformFlag and EXACT_MOVE != 0) {
            pCachedData(buffer, blocks.exactMove)
        }
        if (platformFlag and SEQUENCE != 0) {
            pCachedData(buffer, blocks.sequence)
        }
        if (platformFlag and TINTING != 0) {
            pCachedData(buffer, blocks.tinting)
        }
        if (platformFlag and HEADICON_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.headIconCustomisation)
        }
        if (platformFlag and NAME_CHANGE != 0) {
            pCachedData(buffer, blocks.nameChange)
        }
        if (platformFlag and HEAD_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.headCustomisation)
        }
        if (platformFlag and BODY_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.bodyCustomisation)
        }
        if (platformFlag and TRANSFORMATION != 0) {
            pCachedData(buffer, blocks.transformation)
        }
        if (platformFlag and LEVEL_CHANGE != 0) {
            pCachedData(buffer, blocks.combatLevelChange)
        }
        if (platformFlag and HITS != 0) {
            pOnDemandData(buffer, localIndex, blocks.hit, observerIndex)
        }
        if (platformFlag and FACE_COORD != 0) {
            pCachedData(buffer, blocks.faceCoord)
        }
        if (platformFlag and FACE_PATHINGENTITY != 0) {
            pCachedData(buffer, blocks.facePathingEntity)
        }
        if (platformFlag and BAS_CHANGE != 0) {
            pCachedData(buffer, blocks.baseAnimationSet)
        }
    }

    @Suppress("unused")
    private companion object {
        private const val EXTENDED_SHORT: Int = 0x2
        private const val EXTENDED_MEDIUM: Int = 0x100

        // old spotanim: 0x40
        private const val SPOTANIM: Int = 0x10000
        private const val SAY: Int = 0x1
        private const val OPS: Int = 0x4000
        private const val EXACT_MOVE: Int = 0x2000
        private const val SEQUENCE: Int = 0x20
        private const val TINTING: Int = 0x1000
        private const val HEADICON_CUSTOMISATION: Int = 0x20000
        private const val NAME_CHANGE: Int = 0x8000
        private const val HEAD_CUSTOMISATION: Int = 0x400
        private const val BODY_CUSTOMISATION: Int = 0x200
        private const val TRANSFORMATION: Int = 0x10
        private const val LEVEL_CHANGE: Int = 0x800
        private const val HITS: Int = 0x4
        private const val FACE_COORD: Int = 0x8
        private const val FACE_PATHINGENTITY: Int = 0x80
        private const val BAS_CHANGE: Int = 0x40000
    }
}
