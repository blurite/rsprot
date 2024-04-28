package net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The temporary move speed is used to set a move speed for a single cycle, commonly done
 * when the player has run enabled through the orb, but decides to only walk a single tile instead.
 * Rather than to switch the main mode over to walking, it utilizes the temporary move speed
 * so the primary one will remain as running after this one cycle, as they are far more likely
 * to utilize the move speed described by their run orb.
 * @param encoders the array of client-specific encoders for temporary move speed.
 */
public class TemporaryMoveSpeed(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>>,
) : TransientExtendedInfo<TemporaryMoveSpeed, PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>>() {
    /**
     * The movement speed of this avatar for a single cycle.
     */
    public var value: Int = -1

    override fun clear() {
        releaseBuffers()
        value = -1
    }
}
