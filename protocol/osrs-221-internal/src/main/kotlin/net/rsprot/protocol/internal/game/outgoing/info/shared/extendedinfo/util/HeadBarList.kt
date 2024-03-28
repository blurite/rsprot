package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

public class HeadBarList(
    private val elements: MutableList<HeadBar>,
) : MutableList<HeadBar> by elements {
    public constructor(capacity: Int = DEFAULT_CAPACITY) : this(ArrayList(capacity))

    private companion object {
        private const val DEFAULT_CAPACITY = 10
    }
}
