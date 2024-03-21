package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.game.outgoing.info.util.Avatar
import net.rsprot.protocol.game.outgoing.info.util.CoordGrid

public data class PlayerAvatar internal constructor(
    /**
     * The preferred resize range. The player information protocol will attempt to
     * add everyone within [preferredResizeRange] tiles to high resolution.
     * If [preferredResizeRange] is equal to [Int.MAX_VALUE], resizing will be disabled
     * and everyone will be put to high resolution. The extended information may be
     * disabled for these players as a result, to avoid buffer overflows.
     */
    private var preferredResizeRange: Int = DEFAULT_RESIZE_RANGE,
    /**
     * The current range at which other players can be observed.
     * By default, this value is equal to 15 game squares, however, it may dynamically
     * decrease if there are too many high resolution players nearby. It will naturally
     * restore back to the default size when the pressure starts to decrease.
     */
    private var resizeRange: Int = preferredResizeRange,
    /**
     * The current cycle counter for resizing logic.
     * Resizing by default will occur after every ten cycles. Once the
     * protocol begins decrementing the range, it will continue to do so
     * every cycle until it reaches a low enough pressure point.
     * Every 11th cycle from thereafter, it will attempt to increase it back.
     * If it succeeds, it will continue to do so every cycle, similarly to decreasing.
     * If it however fails, it will set the range lower by one tile and remain there
     * for the next ten cycles.
     */
    private var resizeCounter: Int = DEFAULT_RESIZE_INTERVAL,
    /**
     * The current known coordinate of the given player.
     * The coordinate property will need to be updated for all players prior to computing
     * player info packet for any of them.
     */
    private var currentCoord: CoordGrid = CoordGrid.INVALID,
    /**
     * The last known coordinate of this player. This property will be used in conjunction
     * with [currentCoord] to determine the coordinate delta, which is then transmitted
     * to the clients.
     */
    private var lastCoord: CoordGrid = CoordGrid.INVALID,
) : Avatar {
    internal fun reset() {
        preferredResizeRange = DEFAULT_RESIZE_RANGE
        resizeRange = preferredResizeRange
        resizeCounter = DEFAULT_RESIZE_INTERVAL
        currentCoord = CoordGrid.INVALID
        lastCoord = CoordGrid.INVALID
    }

    override fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.currentCoord = CoordGrid(level, x, z)
    }

    override fun postUpdate() {
        this.lastCoord = currentCoord
    }

    private companion object {
        /**
         * The default range of visibility of other players, in game tiles.
         */
        private const val DEFAULT_RESIZE_RANGE = 15

        /**
         * The default interval at which resizing will be checked, in game cycles.
         */
        private const val DEFAULT_RESIZE_INTERVAL = 10
    }
}
