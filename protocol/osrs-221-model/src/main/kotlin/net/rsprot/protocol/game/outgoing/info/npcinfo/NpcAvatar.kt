package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
import net.rsprot.protocol.game.outgoing.info.npcinfo.util.NpcCellOpcodes
import net.rsprot.protocol.game.outgoing.info.util.Avatar
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.NpcAvatarDetails
import java.util.concurrent.atomic.AtomicInteger

public class NpcAvatar internal constructor(
    index: Int,
    id: Int,
    /**
     * Extended info repository, commonly referred to as "masks", will track everything relevant
     * inside itself. Setting properties such as a spotanim would be done through this.
     * The [extendedInfo] is also responsible for caching the non-temporary blocks,
     * such as appearance and move speed.
     */
    public val extendedInfo: NpcAvatarExtendedInfo,
) : Avatar {
    public val details: NpcAvatarDetails = NpcAvatarDetails(index, id)
    private val observerCount: AtomicInteger = AtomicInteger()

    internal var highResMovementBuffer: UnsafeLongBackedBitBuf? = null

    internal fun addObserver() {
        observerCount.incrementAndGet()
    }

    internal fun removeObserver() {
        observerCount.decrementAndGet()
    }

    internal fun hasObservers(): Boolean {
        return observerCount.get() > 0
    }

    public fun teleport(
        level: Int,
        x: Int,
        z: Int,
        jump: Boolean,
    ) {
        details.currentCoord = CoordGrid(level, x, z)
        details.movementType = details.movementType or (if (jump) NpcAvatarDetails.TELEJUMP else NpcAvatarDetails.TELE)
    }

    public fun crawl(
        deltaX: Int,
        deltaZ: Int,
    ) {
        singleStepMovement(
            deltaX,
            deltaZ,
            NpcAvatarDetails.CRAWL,
        )
    }

    public fun walk(
        deltaX: Int,
        deltaZ: Int,
    ) {
        singleStepMovement(
            deltaX,
            deltaZ,
            NpcAvatarDetails.WALK,
        )
    }

    private fun singleStepMovement(
        deltaX: Int,
        deltaZ: Int,
        flag: Int,
    ) {
        val opcode = NpcCellOpcodes.singleCellMovementOpcode(deltaX, deltaZ)
        val (level, x, z) = details.currentCoord
        details.currentCoord = CoordGrid(level, x + deltaX, z + deltaZ)
        when (++details.stepCount) {
            1 -> {
                details.firstStep = opcode
                details.movementType = details.movementType or flag
            }
            2 -> {
                details.secondStep = opcode
                details.movementType = details.movementType or NpcAvatarDetails.RUN
            }
            else -> {
                details.movementType = details.movementType or NpcAvatarDetails.TELE
            }
        }
    }

    internal fun prepareBitcodes() {
        val movementType = details.movementType
        // If teleporting, or if there are no observers, there's no need to compute this
        if (movementType and (NpcAvatarDetails.TELE or NpcAvatarDetails.TELEJUMP) != 0 || observerCount.get() == 0) {
            return
        }
        val buffer = UnsafeLongBackedBitBuf()
        this.highResMovementBuffer = buffer
        val extendedInfo = this.extendedInfo.flags != 0
        if (movementType and NpcAvatarDetails.RUN != 0) {
            pRun(buffer, extendedInfo)
        } else if (movementType and NpcAvatarDetails.WALK != 0) {
            pWalk(buffer, extendedInfo)
        } else if (movementType and NpcAvatarDetails.CRAWL != 0) {
            pCrawl(buffer, extendedInfo)
        } else if (extendedInfo) {
            pExtendedInfo(buffer)
        } else {
            pNoUpdate(buffer)
        }
    }

    private fun pNoUpdate(buffer: UnsafeLongBackedBitBuf) {
        buffer.pBits(1, 0)
    }

    private fun pExtendedInfo(buffer: UnsafeLongBackedBitBuf) {
        buffer.pBits(1, 0)
        buffer.pBits(2, 0)
    }

    private fun pCrawl(
        buffer: UnsafeLongBackedBitBuf,
        extendedInfo: Boolean,
    ) {
        buffer.pBits(1, 1)
        buffer.pBits(2, 2)
        buffer.pBits(1, 0)
        buffer.pBits(3, details.firstStep)
        buffer.pBits(1, if (extendedInfo) 1 else 0)
    }

    private fun pWalk(
        buffer: UnsafeLongBackedBitBuf,
        extendedInfo: Boolean,
    ) {
        buffer.pBits(1, 1)
        buffer.pBits(2, 1)
        buffer.pBits(3, details.firstStep)
        buffer.pBits(1, if (extendedInfo) 1 else 0)
    }

    private fun pRun(
        buffer: UnsafeLongBackedBitBuf,
        extendedInfo: Boolean,
    ) {
        buffer.pBits(1, 1)
        buffer.pBits(2, 2)
        buffer.pBits(1, 1)
        buffer.pBits(3, details.firstStep)
        buffer.pBits(3, details.secondStep)
        buffer.pBits(1, if (extendedInfo) 1 else 0)
    }

    override fun postUpdate() {
        details.lastCoord = details.currentCoord
        details.stepCount = 0
        details.firstStep = -1
        details.secondStep = -1
        details.movementType = 0
    }
}
