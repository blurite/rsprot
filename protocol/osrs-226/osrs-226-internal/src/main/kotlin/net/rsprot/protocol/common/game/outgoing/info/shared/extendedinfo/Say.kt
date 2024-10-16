package net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The say extended info block tracks any overhead chat set by the server,
 * through content. Public chat will not utilize this.
 * @param encoders the array of client-specific encoders for say.
 */
public class Say(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Say>>,
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
