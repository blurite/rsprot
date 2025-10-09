@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.worldentity.extendedinfo

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarExtendedInfo
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarExtendedInfoBlocks
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder.WorldEntityExtendedInfoEncoders

public class WorldEntityAvatarExtendedInfoDesktopWriter :
    AvatarExtendedInfoWriter<WorldEntityExtendedInfoEncoders, WorldEntityAvatarExtendedInfoBlocks>(
        OldSchoolClientType.DESKTOP,
        WorldEntityExtendedInfoEncoders(
            OldSchoolClientType.DESKTOP,
            WorldEntityVisibleOpsEncoder(),
        ),
    ) {
    private fun convertFlags(constantFlags: Int): Int {
        var clientFlags = 0
        if (constantFlags and WorldEntityAvatarExtendedInfo.VISIBLE_OPS != 0) {
            clientFlags = clientFlags or VISIBLE_OPS
        }
        return clientFlags
    }

    @Suppress("DuplicatedCode")
    override fun pExtendedInfo(
        buffer: JagByteBuf,
        localIndex: Int,
        observerIndex: Int,
        flag: Int,
        blocks: WorldEntityAvatarExtendedInfoBlocks,
    ) {
        val clientFlag = convertFlags(flag)
        var outFlag = clientFlag
        val flagIndex = buffer.writerIndex()

        buffer.p1(clientFlag)

        // sequence; unused
        outFlag = outFlag or pCached(buffer, clientFlag, VISIBLE_OPS, blocks.visibleOps)

        if (outFlag != clientFlag) {
            val finalPos = buffer.writerIndex()
            buffer.writerIndex(flagIndex)
            buffer.p1(outFlag)
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

    @Suppress("SameParameterValue", "unused")
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
        private const val SEQUENCE: Int = 0x1
        private const val VISIBLE_OPS: Int = 0x2
    }
}
