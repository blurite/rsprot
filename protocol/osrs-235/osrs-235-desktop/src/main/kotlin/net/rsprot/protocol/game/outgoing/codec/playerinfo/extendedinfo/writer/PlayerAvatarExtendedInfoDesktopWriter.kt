@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer

import com.github.michaelbull.logging.InlineLogger
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
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
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
        var outFlag = clientFlag and (EXTENDED_SHORT or EXTENDED_MEDIUM)
        val flagIndex = buffer.writerIndex()

        buffer.p1(clientFlag)
        if (clientFlag and EXTENDED_SHORT != 0) {
            buffer.p1(clientFlag shr 8)
        }
        if (clientFlag and EXTENDED_MEDIUM != 0) {
            buffer.p1(clientFlag shr 16)
        }

        outFlag = outFlag or pCached(buffer, clientFlag, APPEARANCE, blocks.appearance)
        outFlag = outFlag or pCached(buffer, clientFlag, TEMP_MOVE_SPEED, blocks.temporaryMoveSpeed)
        outFlag = outFlag or pOnDemand(buffer, clientFlag, TINTING, blocks.tinting, localIndex, observerIndex)
        outFlag = outFlag or pCached(buffer, clientFlag, FACE_ANGLE, blocks.faceAngle)
        outFlag = outFlag or pCached(buffer, clientFlag, SEQUENCE, blocks.sequence)
        outFlag = outFlag or pCached(buffer, clientFlag, SAY, blocks.say)
        outFlag = outFlag or pCached(buffer, clientFlag, CHAT, blocks.chat)
        outFlag = outFlag or pOnDemand(buffer, clientFlag, HITS, blocks.hit, localIndex, observerIndex)
        outFlag = outFlag or pCached(buffer, clientFlag, MOVE_SPEED, blocks.moveSpeed)
        // Old chat
        outFlag = outFlag or pCached(buffer, clientFlag, SPOTANIM, blocks.spotAnims)
        // Name extras
        outFlag = outFlag or pCached(buffer, clientFlag, EXACT_MOVE, blocks.exactMove)
        outFlag = outFlag or pCached(buffer, clientFlag, FACE_PATHINGENTITY, blocks.facePathingEntity)

        if (outFlag != clientFlag) {
            val finalPos = buffer.writerIndex()
            buffer.writerIndex(flagIndex)
            buffer.p1(outFlag)
            if (outFlag and EXTENDED_SHORT != 0) {
                buffer.p1(outFlag shr 8)
            }
            if (outFlag and EXTENDED_MEDIUM != 0) {
                buffer.p1(outFlag shr 16)
            }
            buffer.writerIndex(finalPos)
        }
    }

    private fun <T : ExtendedInfo<T, E>, E : PrecomputedExtendedInfoEncoder<T>> pCached(
        buffer: JagByteBuf,
        clientFlag: Int,
        blockFlag: Int,
        block: T,
    ): Int {
        if (clientFlag and blockFlag == 0) return 0
        val pos = buffer.writerIndex()
        return try {
            pCachedData(buffer, block)
            blockFlag
        } catch (e: Exception) {
            buffer.writerIndex(pos)
            logger.error(e) {
                "Unable to put cached mask data for $block"
            }
            0
        }
    }

    private fun <T : ExtendedInfo<T, E>, E : OnDemandExtendedInfoEncoder<T>> pOnDemand(
        buffer: JagByteBuf,
        clientFlag: Int,
        blockFlag: Int,
        block: T,
        localIndex: Int,
        observerIndex: Int,
    ): Int {
        if (clientFlag and blockFlag == 0) return 0
        val pos = buffer.writerIndex()
        return try {
            pOnDemandData(buffer, localIndex, block, observerIndex)
            blockFlag
        } catch (e: Exception) {
            buffer.writerIndex(pos)
            logger.error(e) {
                "Unable to put on demand mask data for $block"
            }
            0
        }
    }

    @Suppress("unused")
    private companion object {
        private val logger = InlineLogger()
        private const val EXTENDED_SHORT = 0x1
        private const val EXTENDED_MEDIUM = 0x100

        private const val CHAT_OLD = 0x2
        private const val APPEARANCE = 0x4
        private const val FACE_PATHINGENTITY = 0x8
        private const val SEQUENCE = 0x10
        private const val SAY = 0x20
        private const val HITS = 0x40
        private const val FACE_ANGLE = 0x80
        private const val EXACT_MOVE = 0x200
        private const val MOVE_SPEED = 0x800
        private const val CHAT = 0x1000
        private const val TINTING = 0x2000
        private const val TEMP_MOVE_SPEED = 0x4000
        private const val SPOTANIM = 0x10000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        private const val NAME_EXTRAS = 0x400
    }
}
