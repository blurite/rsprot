package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The movement speed extended info block.
 * Unlike most extended info blocks, the [value] will last as long as the server tells it to.
 * The client will also temporarily cache it for the duration that it sees an avatar in high resolution.
 * Whenever an avatar moves, unless the move speed has been overwritten, this is the speed
 * that it will use for the movement, barring any special mechanics.
 * If an avatar goes from high resolution to low resolution, the client **will not** cache this,
 * and a new status update must be written when the opposite transition occurs.
 * This move speed status should typically be synchronized with the state of the "Run orb".
 * @param encoders the array of client-specific encoders for move speed.
 */
public class MoveSpeed(
	override val encoders: net.rsprot.protocol.internal.client.ClientTypeMap<PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed>>,
) : TransientExtendedInfo<net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed, PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed>>() {
    /**
     * The current movement speed of this avatar.
     */
    public var value: Int =
	    net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed.Companion.DEFAULT_MOVESPEED

    override fun clear() {
        releaseBuffers()
        value =
	        net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed.Companion.DEFAULT_MOVESPEED
    }

    public companion object {
        public const val DEFAULT_MOVESPEED: Int = 0
    }
}
