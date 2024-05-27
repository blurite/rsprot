package net.rsprot.protocol.game.outgoing.info.worldentityinfo

/**
 * An index supplier for world entities.
 */
public fun interface WorldEntityIndexSupplier {
    /**
     * Supplies an iterator of world entity indices that will be added to high resolution,
     * if they are not already in there. Furthermore, a secondary build-area check is performed
     * before such world entities may be added, as a safety precaution.
     * @param localPlayerIndex the index of the local player for whom the indices are
     * being supplied.
     * @param level the height level of the coordinate where to look up world entities.
     * @param x the absolute x coordinate around which to find world entities.
     * @param z the absolute z coordinate around which to find world entities.
     * @param viewDistance the current view distance in tiles.
     */
    public fun supply(
        localPlayerIndex: Int,
        level: Int,
        x: Int,
        z: Int,
        viewDistance: Int,
    ): Iterator<Int>
}
