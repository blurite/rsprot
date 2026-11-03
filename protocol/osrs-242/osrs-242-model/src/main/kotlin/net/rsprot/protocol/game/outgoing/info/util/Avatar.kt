package net.rsprot.protocol.game.outgoing.info.util

public interface Avatar {
    /**
     * Handles any changes to be done to the avatar post its update.
     * This will clean up any extended info blocks and update the last coordinate to
     * match up with the current (set earlier in the cycle).
     */
    public fun postUpdate()
}
