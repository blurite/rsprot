package net.rsprot.protocol.game.outgoing.info.worldentityinfo

/**
 * An enum of possible world entity render priorities.
 * These dictate the "pass" in which the given worldentity will be drawn in the scene.
 * Note that the [OTHER_PLAYER] priority has a special exception in the client which makes it
 * render the entity as a greyed out shadow if a higher priority world overlaps with it.
 */
public enum class WorldEntityPriority(
    public val id: Int,
) {
    NPC(0),
    OTHER_PLAYER(1),
    LOCAL_PLAYER(2),
}
