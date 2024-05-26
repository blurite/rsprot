package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBuf

internal class PlayerInfoWorldDetails(
    internal val worldId: Int,
) {
    internal var initialized: Boolean = false

    /**
     * The flags indicating the status of the players in the previous and current cycles.
     * This is used to categorize players who are 'stationary', which implies they did not
     * move, nor did they have any extended info blocks written for them. By batching
     * players up this way, the protocol is able to skip a larger number of players
     * with each skip block, as players are far more likely to be in the same state
     * as they were in the last cycle.
     */
    internal val stationary = ByteArray(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    /**
     * Low resolution indices are tracked together with [lowResolutionCount].
     * Whenever a player enters the low resolution view, their index
     * is added into this [lowResolutionIndices] array, and the [lowResolutionCount]
     * is incremented by one.
     * At the end of each cycle, the [lowResolutionIndices] are rebuilt to sort the indices.
     */
    internal val lowResolutionIndices: ShortArray = ShortArray(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    /**
     * The number of players in low resolution according to the protocol.
     */
    internal var lowResolutionCount: Int = 0

    /**
     * The tracked high resolution players by their indices.
     * If a player enters our high resolution, the bit at their index is set to true.
     * We do not need to use references to players as we can then refer to the [PlayerInfoRepository]
     * to find the actual [PlayerInfo] implementation.
     */
    internal val highResolutionPlayers: LongArray = LongArray(PlayerInfoProtocol.PROTOCOL_CAPACITY ushr 6)

    /**
     * High resolution indices are tracked together with [highResolutionCount].
     * Whenever an external player enters the high resolution view, their index
     * is added into this [highResolutionIndices] array, and the [highResolutionCount]
     * is incremented by one.
     * At the end of each cycle, the [highResolutionIndices] are rebuilt to sort the indices.
     */
    internal val highResolutionIndices: ShortArray = ShortArray(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    /**
     * The number of players in high resolution according to the protocol.
     */
    internal var highResolutionCount: Int = 0

    /**
     * The extended info indices contain pointers to all the players for whom we need to
     * write an extended info block. We do this rather than directly writing them as this
     * improves CPU cache locality and allows us to batch extended info blocks together.
     */
    internal val extendedInfoIndices: ShortArray = ShortArray(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    /**
     * The number of players for whom we need to write extended info blocks this cycle.
     */
    internal var extendedInfoCount: Int = 0

    /**
     * The buffer into which all the information is written in this cycle.
     * It should be noted that this buffer is constantly changing, as we reallocate
     * a new buffer instance through the [allocator] each cycle. This is to ensure that
     * we do not start overwriting a buffer before it has been fully written into the pipeline.
     * Thus, a pooled [allocator] implementation should be preferred to avoid expensive re-allocations.
     */
    internal var buffer: ByteBuf? = null

    /**
     * Whether the buffer allocated by this player info object has been built
     * into a packet message. If this returns false, but player info was in fact built,
     * we have an allocated buffer that needs releasing. If the NPC info itself
     * is released but isn't built into packet, we make sure to release it, to avoid
     * any memory leaks.
     */
    internal var builtIntoPacket: Boolean = false
}
