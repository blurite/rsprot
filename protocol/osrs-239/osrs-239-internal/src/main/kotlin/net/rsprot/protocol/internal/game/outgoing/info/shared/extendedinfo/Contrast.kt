package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The contrast class is used to gradually apply a contrast over the whole player or npc model,
 * over the timespan provided.
 */
public class Contrast(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Contrast>>,
) : TransientExtendedInfo<Contrast, PrecomputedExtendedInfoEncoder<Contrast>>() {
    /**
     * The delay in client cycles (20ms/cc) until the contrast is applied.
     */
    public var start: Short = 0

    /**
     * The timestamp in client cycles (20ms/cc) until the contrast finishes.
     */
    public var end: Short = 0

    /**
     * The starting contrast value to apply when the [start] client cycle is reached,
     * if [useStartContrast] is true.
     */
    public var startContrast: Byte = 0

    /**
     * The ending contrast to apply once the [end] client cycle is reached.
     */
    public var endContrast: Byte = 0

    /**
     * Whether to use the [startContrast] value, or the current contrast value that
     * already exists.
     */
    public var useStartContrast: Boolean = false

    override fun clear() {
        releaseBuffers()
        start = 0
        end = 0
        startContrast = 0
        endContrast = 0
        useStartContrast = false
    }
}
