package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

public class HitMarkList(
    private val elements: MutableList<HitMark>,
) : MutableList<HitMark> by elements {
    public constructor(capacity: Int = DEFAULT_CAPACITY) : this(ArrayList(capacity))

    private companion object {
        private const val DEFAULT_CAPACITY = 10
    }
}
