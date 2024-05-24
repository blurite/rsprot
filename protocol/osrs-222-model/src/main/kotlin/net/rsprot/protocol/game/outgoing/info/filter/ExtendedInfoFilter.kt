package net.rsprot.protocol.game.outgoing.info.filter

/**
 * Extended info filter provides the protocol with a strategy for how to handle
 * the packet capacity limitations, as it is all too easy to fly past the 40kb
 * limitation in extreme scenarios and benchmarks. This interface is responsible
 * for ensuring that the extended info blocks do not exceed the 40kb limitation.
 * In order to achieve this, all necessary information is provided within the
 * [accept] function. It should be noted that at least 1 byte of space is
 * necessary per each remaining avatar at the very least, as we write the flag
 * as zero in those extreme scenarios.
 */
public fun interface ExtendedInfoFilter {
    /**
     * Whether to accept writing the extended info blocks for the next avatar.
     * @param writableBytes the amount of bytes that can still be written into the buffer
     * before reaching its absolute capacity. 1 byte of space is required as a minimum
     * per each [remainingAvatars].
     * @param constantFlag the bitpacked flag of all the extended info blocks flagged
     * for this avatar. This function utilizes the constant flags found in
     * [net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfo],
     * rather than the client-specific variants.
     * @param remainingAvatars the number of avatars for whom we need to still write
     * extended info blocks. This includes the current avatar on whom we are checking
     * the accept function. Per each avatar, at least one byte must be writable.
     * @param previouslyObserved whether the protocol has previously observed this
     * avatar. This is done by checking if our appearance cache has previously tracked
     * an avatar by that index. While the exact acceptation mechanics are unknown,
     * in times of high pressure, OldSchool RuneScape seems to always send extended info
     * about the avatars whom you've already observed in the past. However, it is very
     * strict with whom it newly accepts, often only rendering 16 or 32 players
     * when there's high resolution information sent about a thousand of them.
     */
    public fun accept(
        writableBytes: Int,
        constantFlag: Int,
        remainingAvatars: Int,
        previouslyObserved: Boolean,
    ): Boolean
}
