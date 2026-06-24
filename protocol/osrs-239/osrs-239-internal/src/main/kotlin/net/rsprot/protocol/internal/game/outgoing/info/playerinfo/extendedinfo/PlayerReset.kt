package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * Player reset extended info is used to reset various state in the client for the player.
 * @param encoders the array of client-specific encoders for player reset.
 */
public class PlayerReset(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<PlayerReset>>,
) : TransientExtendedInfo<PlayerReset, PrecomputedExtendedInfoEncoder<PlayerReset>>() {
    /**
     * A value to transmit to the client. The client fully discards it.
     * The assumption is that Jagex's protocol requires them to have at least
     * one byte written to extended info.
     */
    public var value: UByte = 0u

    override fun clear() {
        releaseBuffers()
        value = 0u
    }
}
