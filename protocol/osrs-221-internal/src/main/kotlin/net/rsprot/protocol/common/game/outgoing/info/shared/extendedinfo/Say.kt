package net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.platform.PlatformMap

/**
 * The say extended info block tracks any overhead chat set by the server,
 * through content. Public chat will not utilize this.
 * @param encoders the array of platform-specific encoders for say.
 */
public class Say(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<Say>>,
) : TransientExtendedInfo<Say, PrecomputedExtendedInfoEncoder<Say>>() {
    /**
     * The text to render over the avatar.
     */
    public var text: String? = null

    override fun clear() {
        releaseBuffers()
        text = null
    }
}
