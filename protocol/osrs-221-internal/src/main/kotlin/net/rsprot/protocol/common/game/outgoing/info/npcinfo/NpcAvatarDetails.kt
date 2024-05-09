package net.rsprot.protocol.common.game.outgoing.info.npcinfo

import net.rsprot.protocol.common.game.outgoing.info.CoordGrid

/**
 * An internal class holding the state of NPC's avatar, containing everything
 * that is sent during the movement from low to high resolution.
 * @property index the index of the npc in the world
 * @property id the id of the npc in the world
 * @property currentCoord the current coordinate of the npc
 * @property stepCount the number of steps the npc has taken this cycle
 * @property firstStep the direction of the first step that the npc has taken this cycle, or -1
 * @property secondStep the direction of the second step that the npc has taken this cycle, or -2
 * @property movementType the bitpacked flag of all the movement typed the npc has utilized this cycle
 * @property spawnCycle the game cycle on which the npc was originally spawned into the world
 * @property direction the direction that the npc is facing when it is first added to high resolution view
 * @property inaccessible whether the npc is inaccessible to all players, meaning it will not be
 * added to high resolution for anyone, even though it is still within the zone. This is intended
 * to be used with static npcs that respawn. After death, inaccessible should be set to true, and
 * when the npc respawns, it should be set back to false. This allows us to not re-allocate avatars
 * which furthermore requires cleanup and micromanaging.
 */
public class NpcAvatarDetails internal constructor(
    public var index: Int,
    public var id: Int,
    public var currentCoord: CoordGrid = CoordGrid.INVALID,
    public var stepCount: Int = 0,
    public var firstStep: Int = -1,
    public var secondStep: Int = -1,
    public var movementType: Int = 0,
    public var spawnCycle: Int = 0,
    public var direction: Int = 0,
    public var inaccessible: Boolean = false,
) {
    public constructor(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
    ) : this(
        index,
        id,
        CoordGrid(level, x, z),
        spawnCycle = spawnCycle,
        direction = direction,
    )

    /**
     * Whether the npc is tele jumping, meaning it will jump over to the destination
     * coord, even if it is just one tile away.
     */
    public fun isJumping(): Boolean {
        return movementType and TELEJUMP != 0
    }

    /**
     * Whether the npc is teleporting. This means the npc will render as jumping
     * if the destination is > 2 tiles away, and normal walk/run/in-between if the
     * distance is 2 tiles or less.
     */
    public fun isTeleporting(): Boolean {
        return movementType and (TELE or TELEJUMP) != 0
    }

    /**
     * Updates the current direction of the npc, allowing the server to sync up
     * the current faced coordinate of npcs during movement, face angle and such.
     */
    public fun updateDirection(direction: Int) {
        this.direction = direction
    }

    public companion object {
        /**
         * The constant flag movement type indicating the npc did crawl.
         */
        public const val CRAWL: Int = 0x1

        /**
         * The constant flag movement type indicating the npc did walk.
         */
        public const val WALK: Int = 0x2

        /**
         * The constant flag movement type indicating the npc did run.
         * Run state is additionally reached if two walks, two crawls or a mix of
         * a crawl and walk was used in one cycle. More than two walks/crawls will
         * however turn into a telejump.
         * This flag has a higher priority than crawl or walk, but is surpassed by both teleports.
         */
        public const val RUN: Int = 0x4

        /**
         * The constant flag indicating the npc is teleporting without a jump.
         * The jump condition is automatically included if the npc moves more than 2 tiles.
         * This flag has the highest priority out of all above, only surpassed by telejump.
         */
        public const val TELE: Int = 0x8

        /**
         * The constant flag indicating the npc is jumping regardless of distance.
         * This flag has the highest priority out of all above.
         */
        public const val TELEJUMP: Int = 0x10
    }
}
