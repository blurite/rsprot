package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.NpcAvatarDetails
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.game.outgoing.info.npcinfo.util.NpcCellOpcodes
import net.rsprot.protocol.game.outgoing.info.util.Avatar
import java.util.concurrent.atomic.AtomicInteger

/**
 * The npc avatar class represents an NPC as shown by the client.
 * This class contains all the properties necessary to put a NPC into high resolution.
 *
 * Npc direction table:
 * ```
 * | Id | Client Angle |  Direction |
 * |:--:|:------------:|:----------:|
 * |  0 |      768     | North-West |
 * |  1 |     1024     |    North   |
 * |  2 |     1280     | North-East |
 * |  3 |      512     |    West    |
 * |  4 |     1536     |    East    |
 * |  5 |      256     | South-West |
 * |  6 |       0      |    South   |
 * |  7 |     1792     | South-East |
 * ```
 *
 * @param index the index of the npc in the world
 * @param id the id of the npc in the world, limited to range of 0..16383
 * @param level the height level of the npc
 * @param x the absolute x coordinate of the npc
 * @param z the absolute z coordinate of the npc
 * @param spawnCycle the game cycle on which the npc spawned into the world;
 * for static NPCs, this would always be zero. This is only used by the C++ clients.
 * @param direction the direction that the npc will face on spawn (see table above)
 * @property extendedInfo the extended info, commonly referred to as "masks", will track everything relevant
 * inside itself. Setting properties such as a spotanim would be done through this.
 * The [extendedInfo] is also responsible for caching the non-temporary blocks,
 * such as appearance and move speed.
 * @property zoneIndexStorage the storage tracking all the allocated game NPCs based on the zones.
 */
public class NpcAvatar internal constructor(
    index: Int,
    id: Int,
    level: Int,
    x: Int,
    z: Int,
    spawnCycle: Int = 0,
    direction: Int = 0,
    allocateCycle: Int,
    public val extendedInfo: NpcAvatarExtendedInfo,
    public val zoneIndexStorage: ZoneIndexStorage,
) : Avatar {
    /**
     * Npc avatar details class wraps all the client properties of a NPC in its own
     * data structure.
     */
    internal val details: NpcAvatarDetails =
        NpcAvatarDetails(
            index,
            id,
            level,
            x,
            z,
            spawnCycle,
            direction,
            allocateCycle,
        )

    /**
     * The number of player avatars observing this NPC avatar.
     * We utilize the count tracking to determine what NPCs require precomputation.
     * As the game has circa 25,000 NPCs, and even at max world capacity, only 2,000 players,
     * the majority of NPCs in the game will at all times __not__ be observed by any players.
     * This means computing their high resolution blocks is unnecessary, as that is strictly
     * only for players who are already observing a NPC - moving from low resolution to high
     * resolution has its own set of code.
     * Additionally, this is used to skip computing extended info blocks later on in the cycle,
     * given the assumption that no player added this NPC to their high resolution view.
     * Furthermore, this observer count must be an atomic integer, as certain parts of NPC info
     * are multithreaded, including the parts which modify this count.
     */
    private val observerCount: AtomicInteger = AtomicInteger()

    /**
     * The high resolution movement buffer, used to avoid re-calculating the movement information
     * for each observer of a given NPC, in cases where there are multiple. It is additionally
     * more efficient to just do a single bulk pBits() call, than to call it multiple times, which
     * this accomplishes.
     */
    internal var highResMovementBuffer: UnsafeLongBackedBitBuf? = null

    /**
     * Adds an observer to this avatar by incrementing the observer count.
     * Note that it is necessary for servers to de-register npc info when the player is logging off,
     * or the protocol will run into issues on multiple levels.
     */
    internal fun addObserver() {
        observerCount.incrementAndGet()
    }

    /**
     * Removes an observer from this avatar by decrementing the observer count.
     * This function must be called when a player logs off for each NPC they were observing.
     */
    internal fun removeObserver() {
        // If the allocation cycle is the same as current cycle count,
        // a "hotswap" has occurred.
        // This means that a npc was deallocated and another allocated the same index
        // in the same cycle.
        // Due to the new one being allocated, the observer count is already reset
        // to zero, and we cannot decrement the observer count further - it would go negative.
        if (details.allocateCycle == NpcInfoProtocol.cycleCount) {
            return
        }
        observerCount.decrementAndGet()
    }

    /**
     * Checks if this NPC has any observers, necessary to determine whether cached information
     * must be computed for this NPC.
     */
    internal fun hasObservers(): Boolean = observerCount.get() > 0

    /**
     * Resets the observer count.
     */
    internal fun resetObservers() {
        observerCount.set(0)
    }

    /**
     * Updates the spawn direction of the NPC.
     *
     * Table of possible direction values:
     * ```
     * | Id |  Direction | Angle |
     * |:--:|:----------:|:-----:|
     * |  0 | North-West |  768  |
     * |  1 |    North   |  1024 |
     * |  2 | North-East |  1280 |
     * |  3 |    West    |  512  |
     * |  4 |    East    |  1536 |
     * |  5 | South-West |  256  |
     * |  6 |    South   |   0   |
     * |  7 | South-East |  1792 |
     * ```
     *
     * @param direction the direction for the NPC to face.
     */
    public fun updateDirection(direction: Int) {
        require(direction in 0..7) {
            "Direction must be a value in range of 0..7. " +
                "See the table in documentation. Value: $direction"
        }
        this.details.updateDirection(direction)
    }

    /**
     * Sets the id of the avatar - any new observers of this NPC will receive the new id.
     * This should be used in tandem with the transformation extended info block.
     * @param id the id of the npc to set to - any new observers will see that id instead.
     */
    public fun setId(id: Int) {
        require(id in 0..16383) {
            "Id must be a value in range of 0..16383. Value: $id"
        }
        this.details.id = id
    }

    /**
     * A helper function to teleport the NPC to a new coordinate.
     * This will furthermore mark the movement type as teleport, meaning no matter what other
     * coordinate changes are applied, as teleport has the highest priority, teleportation
     * will be how it is rendered on the client's end.
     * @param level the new height level of the NPC
     * @param x the new absolute x coordinate of the NPC
     * @param z the new absolute z coordinate of the NPC
     * @param jump whether to "jump" the NPC to the new coordinate, or to treat it as a
     * regular walk/run type movement. While this should __almost__ always be true, there are
     * certain NPCs, such as Sarachnis in OldSchool, that utilize teleporting without jumping.
     * This effectively makes the NPC appear as it is walking towards the destination. If the
     * NPC falls visually behind, the client will begin increasing its movement speed, to a
     * maximum of run speed, until it has caught up visually.
     */
    public fun teleport(
        level: Int,
        x: Int,
        z: Int,
        jump: Boolean,
    ) {
        zoneIndexStorage.remove(details.index, details.currentCoord)
        details.currentCoord = CoordGrid(level, x, z)
        zoneIndexStorage.add(details.index, details.currentCoord)
        details.movementType = details.movementType or (if (jump) NpcAvatarDetails.TELEJUMP else NpcAvatarDetails.TELE)
    }

    /**
     * Marks the NPC as moved with the crawl movement type.
     * If more than one crawl/walks are sent in one cycle, it will instead be treated as run.
     * If more than two crawl/walks are sent in one cycle, it will be treated as a teleport.
     * @param deltaX the x coordinate delta that the NPC moved.
     * @param deltaZ the z coordinate delta that the npc moved.
     * @throws ArrayIndexOutOfBoundsException if either of the deltas is not in range of -1..1,
     * or both are 0s.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
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

    /**
     * Marks the NPC as moved with the walk movement type.
     * If more than one crawl/walks are sent in one cycle, it will instead be treated as run.
     * If more than two crawl/walks are sent in one cycle, it will be treated as a teleport.
     * @param deltaX the x coordinate delta that the NPC moved.
     * @param deltaZ the z coordinate delta that the npc moved.
     * @throws ArrayIndexOutOfBoundsException if either of the deltas is not in range of -1..1,
     * or both are 0s.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
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

    /**
     * Determines the movement opcode for the NPC, adjusting the NPC's underlying coordinate afterwards,
     * and defines the movement speed based on previous movements in this cycle, as well as the
     * [flag] requested by the movement.
     * @param deltaX the x coordinate delta that the NPC moved.
     * @param deltaZ the z coordinate delta that the npc moved.
     * @param flag the movement speed flag, used to determine what movement speeds have been used
     * in one cycle, given it is possible to move a NPC more than one in one cycle, should the
     * server request it.
     * @throws ArrayIndexOutOfBoundsException if either of the deltas is not in range of -1..1,
     * or both are 0s.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    private fun singleStepMovement(
        deltaX: Int,
        deltaZ: Int,
        flag: Int,
    ) {
        val opcode = NpcCellOpcodes.singleCellMovementOpcode(deltaX, deltaZ)
        val (level, x, z) = details.currentCoord
        zoneIndexStorage.remove(details.index, details.currentCoord)
        details.currentCoord = CoordGrid(level, x + deltaX, z + deltaZ)
        zoneIndexStorage.add(details.index, details.currentCoord)
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

    /**
     * Prepares the bitcodes of a NPC given the assumption it has at least one player observing it,
     * and the NPC is not teleporting (or tele-jumping), as both of those cause it to be treated as
     * remove + re-add client-side, meaning no normal block is used.
     * While it is possible to additionally make NPC removal as part of this function,
     * because part of the responsibility is at the NPC info protocol level (coordinate checks,
     * state checks), it is not possible to fully cover it, so best to leave that for the protocol
     * to handle.
     */
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

    /**
     * Informs the client that there will be no movement or extended info update for this NPC.
     * @param buffer the pre-computed buffer into which to write the bitcodes.
     */
    private fun pNoUpdate(buffer: UnsafeLongBackedBitBuf) {
        buffer.pBits(1, 0)
    }

    /**
     * Informs the client that there is no movement occurring for this NPC, but it does have
     * extended info blocks encoded.
     * @param buffer the pre-computed buffer into which to write the bitcodes.
     */
    private fun pExtendedInfo(buffer: UnsafeLongBackedBitBuf) {
        buffer.pBits(1, 1)
        buffer.pBits(2, 0)
    }

    /**
     * Informs the client that there is a crawl-speed movement occurring for this NPC.
     * @param buffer the pre-computed buffer into which to write the bitcodes.
     * @param extendedInfo whether this NPC additionally has extended info updates coming.
     */
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

    /**
     * Informs the client that there is a walk-speed movement occurring for this NPC.
     * @param buffer the pre-computed buffer into which to write the bitcodes.
     * @param extendedInfo whether this NPC additionally has extended info updates coming.
     */
    private fun pWalk(
        buffer: UnsafeLongBackedBitBuf,
        extendedInfo: Boolean,
    ) {
        buffer.pBits(1, 1)
        buffer.pBits(2, 1)
        buffer.pBits(3, details.firstStep)
        buffer.pBits(1, if (extendedInfo) 1 else 0)
    }

    /**
     * Informs the client that there is a run-speed movement occurring for this NPC.
     * @param buffer the pre-computed buffer into which to write the bitcodes.
     * @param extendedInfo whether this NPC additionally has extended info updates coming.
     */
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

    /**
     * The current height level of this avatar.
     */
    public fun level(): Int = details.currentCoord.level

    /**
     * The current absolute x coordinate of this avatar.
     */
    public fun x(): Int = details.currentCoord.x

    /**
     * The current absolute z coordinate of this avatar.
     */
    public fun z(): Int = details.currentCoord.z

    /**
     * Sets this avatar inaccessible, meaning no player can observe this NPC,
     * but they are still in the world. This is how NPCs in the 'dead' state
     * will be handled.
     * @param inaccessible whether the npc is inaccessible to all players (not rendered)
     */
    public fun setInaccessible(inaccessible: Boolean) {
        details.inaccessible = inaccessible
    }

    /**
     * Checks whether a npc is actively observed by at least one player.
     * @return true if the NPC has at least one player currently observing it via
     * NPC info, false otherwise.
     */
    public fun isActive(): Boolean = observerCount.get() > 0

    /**
     * Checks the number of players that are currently observing this NPC avatar.
     * @return the number of players that are observing this avatar.
     */
    public fun getObserverCount(): Int = observerCount.get()

    override fun postUpdate() {
        details.stepCount = 0
        details.firstStep = -1
        details.secondStep = -1
        details.movementType = 0
        extendedInfo.postUpdate()
    }

    override fun toString(): String =
        "NpcAvatar(" +
            "extendedInfo=$extendedInfo, " +
            "details=$details, " +
            "observerCount=$observerCount, " +
            "highResMovementBuffer=$highResMovementBuffer" +
            ")"
}
