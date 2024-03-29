package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class Say(
    encoders: Array<PrecomputedExtendedInfoEncoder<Say>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<Say, PrecomputedExtendedInfoEncoder<Say>>(encoders) {
    public var text: String? = null

    override fun clear() {
        releaseBuffers()
        text = null
    }
}
