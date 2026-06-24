package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

public class BodyCustomisation(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<BodyCustomisation>>,
) : TransientExtendedInfo<BodyCustomisation, PrecomputedExtendedInfoEncoder<BodyCustomisation>>() {
    public var customisation: TypeCustomisation? = null
    public var composition: PlayerComposition? = null

    public fun isPresent(): Boolean {
        return customisation != null || composition != null
    }

    public fun getOrCreateComposition(): PlayerComposition {
        val composition = this.composition
        if (composition != null) {
            return composition
        }
        this.customisation = null
        val newComposition = PlayerComposition()
        this.composition = newComposition
        return newComposition
    }

    override fun clear() {
        releaseBuffers()
        this.customisation = null
        this.composition = null
    }
}
