package net.rsprot.protocol.game.outgoing.info.npcinfo

/**
 * An optional filter that must be passed before moving a NPC from
 * low resolution to high resolution for a given player, and to keep
 * a NPC in high resolution after the fact.
 * An example of this is hiding any NPCs which would be morphed to
 * id -1 as a result of a specific varbit or varp value.
 *
 * This filter should ideally be efficient, as it is hit a lot.
 * Furthermore, it must be thread-safe, as it will potentially be called
 * from multiple different threads, depending on the threading used by the server.
 */
public fun interface NpcAvatarFilter {
    /**
     * Whether to accept a specific NPC into high resolution view.
     * Note that this filter is invoked last, when every other check
     * has already passed.
     *
     * @param playerIndex the index of the player whose npc info is doing the check.
     * @param npcIndex the index of the npc that will be added to high resolution,
     * if the filter passes.
     * @return whether to transmit the NPC to the client in high resolution.
     */
    public fun accept(
        playerIndex: Int,
        npcIndex: Int,
    ): Boolean
}
