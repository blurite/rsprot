package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The extended info block to make avatars face-lock onto another avatar, be it a NPC or a player.
 * @param encoders the array of client-specific encoders for face pathing entity.
 */
public class FacePathingEntity(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<FacePathingEntity>>,
) : TransientExtendedInfo<FacePathingEntity, PrecomputedExtendedInfoEncoder<FacePathingEntity>>() {
    /**
     * The index of the avatar to face-lock onto. For player avatars,
     * a value of 0x10000 is added onto the index to differentiate it.
     */
    public var index: Int = DEFAULT_VALUE

    override fun clear() {
        releaseBuffers()
        index = DEFAULT_VALUE
    }

    public companion object {
        public const val DEFAULT_VALUE: Int = -1
    }
}
