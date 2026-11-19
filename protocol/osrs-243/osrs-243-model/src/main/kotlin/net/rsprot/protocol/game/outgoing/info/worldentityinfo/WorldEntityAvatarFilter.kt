package net.rsprot.protocol.game.outgoing.info.worldentityinfo

/**
 * An optional filter that must be passed before moving a world entity from
 * low resolution to high resolution for a given player, and to keep
 * a world entity in high resolution after the fact.
 *
 * This filter should ideally be efficient, as it is hit a lot.
 * Furthermore, it must be thread-safe, as it will potentially be called
 * from multiple different threads, depending on the threading used by the server.
 */
public fun interface WorldEntityAvatarFilter {
    /**
     * Whether to accept a specific world entity into high resolution view.
     * This filter is invoked after the range and specific visibility checks,
     * before selecting the highest priority world entities.
     *
     * @param playerIndex the index of the player whose world entity info is doing the check.
     * @param worldEntityIndex the index of the world entity to add or keep in high resolution.
     * @return whether to transmit the world entity to the client in high resolution.
     */
    public fun accept(
        playerIndex: Int,
        worldEntityIndex: Int,
    ): Boolean
}
