package net.rsprot.protocol.game.outgoing.info.worldentityinfo

/**
 * An enum of possible world entity render priorities.
 * These dictate the "pass" in which the given worldentity will be drawn in the scene.
 */
public enum class WorldEntityPriority(
    public val id: Int,
) {
    DEFAULT(0),
    LOW(1),
    HIGH(2),
}
