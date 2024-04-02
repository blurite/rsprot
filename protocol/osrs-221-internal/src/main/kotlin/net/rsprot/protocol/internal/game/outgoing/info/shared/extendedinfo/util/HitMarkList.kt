package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * The hitmark list will contain all the hitmarks for this given avatar for a single cycle,
 * backed by an [ArrayList].
 */
public class HitMarkList(
    private val elements: MutableList<HitMark>,
) : MutableList<HitMark> by elements {
    public constructor(capacity: Int = DEFAULT_CAPACITY) : this(ArrayList(capacity))

    private companion object {
        private const val DEFAULT_CAPACITY = 10
    }
}
