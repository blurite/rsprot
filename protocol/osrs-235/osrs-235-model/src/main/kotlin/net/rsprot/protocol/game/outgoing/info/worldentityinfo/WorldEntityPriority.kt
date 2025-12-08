package net.rsprot.protocol.game.outgoing.info.worldentityinfo

/**
 * An enum of possible world entity render priorities.
 * These dictate the "pass" in which the given worldentity will be drawn in the scene.
 */
public enum class WorldEntityPriority(
    public val id: Int,
) {
    OTHER_PLAYER(0),
    NPC(1),
    LOCAL_PLAYER(2),
}
