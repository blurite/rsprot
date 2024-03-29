package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class Sequence(
    encoders: Array<PrecomputedExtendedInfoEncoder<Sequence>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<Sequence, PrecomputedExtendedInfoEncoder<Sequence>>(encoders) {
    public var id: UShort = 0xFFFFu
    public var delay: UShort = 0u

    override fun clear() {
        releaseBuffers()
        id = 0xFFFFu
        delay = 0u
    }
}
