package net.rsprot.protocol.game.outgoing.info

/**
 * An enum defining the possible avatar priority values.
 * For players, the [LOW] priority is the default.
 * For NPCs, the [NORMAL] priority is the default.
 */
public enum class AvatarPriority(
    public val bitcode: Int,
) {
    LOW(0x0),
    NORMAL(0x1),
}
