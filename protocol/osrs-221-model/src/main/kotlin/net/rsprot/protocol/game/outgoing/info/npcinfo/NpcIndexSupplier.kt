package net.rsprot.protocol.game.outgoing.info.npcinfo

public fun interface NpcIndexSupplier {
    /**
     * The supply function should yield **all** npc indices that are
     * in range of the local player and should be rendered to them.
     * It is important to note that the server will be unaware of the indices
     * that are already tracked by a given player's npc info; the npc info protocol
     * is responsible for ignoring NPCs it already tracks in such cases.
     * Additionally, the protocol is responsible for taking as many indices as it
     * can realistically process. This means that the iterator may be left in
     * a partially-consumed state.
     *
     * One additional side note, because all the indices will be in range of 0..<65535,
     * setting the VM flag `-XX:AutoBoxCacheMax=65535` will help reduce garbage creation,
     * as all the indices will perfectly fit into the integer autobox cache.
     *
     * @param localPlayerIndex the index of the local player, in case further checks
     * are needed to be executed for that player.
     * @param level the height level at which the local player is
     * @param x the x coordinate at which the local player is
     * @param z the z coordinate at which the local player is
     * @param viewDistance the radius how far the local player should be able to see
     * other NPCs, inclusive.
     * @return an iterator that provides all the NPC indices within [viewDistance] range
     * of the local player. For emulation purposes, the iteration should begin with the
     * south-westernmost zone, going north, then east, ie this pattern (in ascending order):
     * ```
     * - - - - -
     * | 3 6 9 |
     * | 2 5 8 |
     * | 1 4 7 |
     * - - - - -
     * ```
     *
     * As for indexing within each zone, the oldest NPCs should be returned first, meaning
     * a natural ascending order.
     * Furthermore, as `& 65535` is performed on each index, values of -1/65535 will be skipped
     * in processing, if there is a need to yield an invalid index for whatever reason.
     * It is important to note that indices which are out of bounds will have the higher
     * order bits ignored, and will likely result in a crash within the protocol.
     */
    public fun supply(
        localPlayerIndex: Int,
        level: Int,
        x: Int,
        z: Int,
        viewDistance: Int,
    ): Iterator<Int>
}
