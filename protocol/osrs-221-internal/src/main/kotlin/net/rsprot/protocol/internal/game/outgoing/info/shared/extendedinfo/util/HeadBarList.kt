package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * The headbar list will contain all the headbars for this given avatar for a single cycle,
 * backed by an [ArrayList].
 */
public class HeadBarList(
    private val elements: MutableList<HeadBar>,
) : MutableList<HeadBar> by elements {
    public constructor(capacity: Int = DEFAULT_CAPACITY) : this(ArrayList(capacity))

    private companion object {
        /**
         * The default capacity for the hits.
         * As most avatars will not be getting hit much, there isn't much reason to
         * allocate a large capacity list ahead of time.
         */
        private const val DEFAULT_CAPACITY = 10
    }
}
