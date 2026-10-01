package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The transparency class is used to gradually apply a transparency over the whole player or npc model,
 * over the timespan provided.
 */
public class Transparency(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Transparency>>,
) : TransientExtendedInfo<Transparency, PrecomputedExtendedInfoEncoder<Transparency>>() {
    /**
     * The delay in client cycles (20ms/cc) until the transparency is applied.
     */
    public var start: Short = 0

    /**
     * The timestamp in client cycles (20ms/cc) until the transparency finishes.
     */
    public var end: Short = 0

    /**
     * The starting transparency value to apply when the [start] client cycle is reached,
     * if [useStartTransparency] is true.
     */
    public var startTransparency: Byte = 0

    /**
     * The ending transparency to apply once the [end] client cycle is reached.
     */
    public var endTransparency: Byte = 0

    /**
     * Whether to use the [startTransparency] value, or the current transparency value that
     * already exists.
     */
    public var useStartTransparency: Boolean = false

    override fun clear() {
        releaseBuffers()
        start = 0
        end = 0
        startTransparency = 0
        endTransparency = 0
        useStartTransparency = false
    }
}
