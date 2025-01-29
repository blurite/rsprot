package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
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

@Suppress("DuplicatedCode")
public class NpcAvatarExtendedInfoDesktopWriter :
    AvatarExtendedInfoWriter<NpcExtendedInfoEncoders, NpcAvatarExtendedInfoBlocks>(
        OldSchoolClientType.DESKTOP,
        NpcExtendedInfoEncoders(
            OldSchoolClientType.DESKTOP,
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
        var clientFlags = 0
        if (constantFlags and NpcAvatarExtendedInfo.FACE_PATHINGENTITY != 0) {
            clientFlags = clientFlags or FACE_PATHINGENTITY
        }
        if (constantFlags and NpcAvatarExtendedInfo.TINTING != 0) {
            clientFlags = clientFlags or TINTING
        }
        if (constantFlags and NpcAvatarExtendedInfo.SAY != 0) {
            clientFlags = clientFlags or SAY
        }
        if (constantFlags and NpcAvatarExtendedInfo.HITS != 0) {
            clientFlags = clientFlags or HITS
        }
        if (constantFlags and NpcAvatarExtendedInfo.SEQUENCE != 0) {
            clientFlags = clientFlags or SEQUENCE
        }
        if (constantFlags and NpcAvatarExtendedInfo.EXACT_MOVE != 0) {
            clientFlags = clientFlags or EXACT_MOVE
        }
        if (constantFlags and NpcAvatarExtendedInfo.SPOTANIM != 0) {
            clientFlags = clientFlags or SPOTANIM
        }
        if (constantFlags and NpcAvatarExtendedInfo.FACE_COORD != 0) {
            clientFlags = clientFlags or FACE_COORD
        }
        if (constantFlags and NpcAvatarExtendedInfo.TRANSFORMATION != 0) {
            clientFlags = clientFlags or TRANSFORMATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.BODY_CUSTOMISATION != 0) {
            clientFlags = clientFlags or BODY_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.HEAD_CUSTOMISATION != 0) {
            clientFlags = clientFlags or HEAD_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.LEVEL_CHANGE != 0) {
            clientFlags = clientFlags or LEVEL_CHANGE
        }
        if (constantFlags and NpcAvatarExtendedInfo.OPS != 0) {
            clientFlags = clientFlags or OPS
        }
        if (constantFlags and NpcAvatarExtendedInfo.NAME_CHANGE != 0) {
            clientFlags = clientFlags or NAME_CHANGE
        }
        if (constantFlags and NpcAvatarExtendedInfo.HEADICON_CUSTOMISATION != 0) {
            clientFlags = clientFlags or HEADICON_CUSTOMISATION
        }
        if (constantFlags and NpcAvatarExtendedInfo.BAS_CHANGE != 0) {
            clientFlags = clientFlags or BAS_CHANGE
        }
        return clientFlags
    }

    override fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: NpcAvatarExtendedInfoBlocks,
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

        // old spotanim
        if (clientFlag and SPOTANIM != 0) {
            pCachedData(buffer, blocks.spotAnims)
        }
        if (clientFlag and SAY != 0) {
            pCachedData(buffer, blocks.say)
        }
        if (clientFlag and OPS != 0) {
            pCachedData(buffer, blocks.visibleOps)
        }
        if (clientFlag and EXACT_MOVE != 0) {
            pCachedData(buffer, blocks.exactMove)
        }
        if (clientFlag and SEQUENCE != 0) {
            pCachedData(buffer, blocks.sequence)
        }
        if (clientFlag and TINTING != 0) {
            pCachedData(buffer, blocks.tinting)
        }
        if (clientFlag and HEADICON_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.headIconCustomisation)
        }
        if (clientFlag and NAME_CHANGE != 0) {
            pCachedData(buffer, blocks.nameChange)
        }
        if (clientFlag and HEAD_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.headCustomisation)
        }
        if (clientFlag and BODY_CUSTOMISATION != 0) {
            pCachedData(buffer, blocks.bodyCustomisation)
        }
        if (clientFlag and TRANSFORMATION != 0) {
            pCachedData(buffer, blocks.transformation)
        }
        if (clientFlag and LEVEL_CHANGE != 0) {
            pCachedData(buffer, blocks.combatLevelChange)
        }
        if (clientFlag and HITS != 0) {
            pOnDemandData(buffer, localIndex, blocks.hit, observerIndex)
        }
        if (clientFlag and FACE_COORD != 0) {
            pCachedData(buffer, blocks.faceCoord)
        }
        if (clientFlag and FACE_PATHINGENTITY != 0) {
            pCachedData(buffer, blocks.facePathingEntity)
        }
        if (clientFlag and BAS_CHANGE != 0) {
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
