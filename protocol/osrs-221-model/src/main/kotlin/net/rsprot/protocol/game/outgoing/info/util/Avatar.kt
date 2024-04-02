package net.rsprot.protocol.game.outgoing.info.util

public interface Avatar {
    /**
     * Updates the current known coordinate of the given [Avatar].
     * This function must be called on each avatar before player info is computed.
     * @param level the current height level of the avatar.
     * @param x the x coordinate of the avatar.
     * @param z the z coordinate of the avatar (this is commonly referred to as 'y' coordinate).
     * @throws IllegalArgumentException if [level] is not in range of 0..<4, or [x]/[z] are
     * not in range of 0..<16384.
     */
    @Throws(IllegalArgumentException::class)
    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    )

    /**
     * Handles any changes to be done to the avatar post its update.
     * This will clean up any extended info blocks and update the last coordinate to
     * match up with the current (set earlier in the cycle).
     */
    public fun postUpdate()
}
