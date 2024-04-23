package net.rsprot.protocol.common.game.outgoing.info.npcinfo

import net.rsprot.protocol.common.game.outgoing.info.CoordGrid

public class NpcAvatarDetails internal constructor(
    public var index: Int,
    // sync with type changes
    public var id: Int,
    public var currentCoord: CoordGrid = CoordGrid.INVALID,
    public var lastCoord: CoordGrid = CoordGrid.INVALID,
    public var stepCount: Int = 0,
    public var firstStep: Int = -1,
    public var secondStep: Int = -1,
    public var movementType: Int = 0,
    public var spawnCycle: Int = 0,
    // 768, 1024, 1280, 512, 1536, 256, 0, 1792
    // TODO: Figure out a nice design for direction
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
        CoordGrid(level, x, z),
        spawnCycle = spawnCycle,
        direction = direction,
    )

    public fun isJumping(): Boolean {
        return movementType and TELEJUMP != 0
    }

    public fun isTeleporting(): Boolean {
        return movementType and (TELE or TELEJUMP) != 0
    }

    public companion object {
        public const val CRAWL: Int = 0x1
        public const val WALK: Int = 0x2
        public const val RUN: Int = 0x4
        public const val TELE: Int = 0x8
        public const val TELEJUMP: Int = 0x10
    }
}
