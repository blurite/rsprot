package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * The hitmark list will contain all the hitmarks for this given avatar for a single cycle,
 * backed by an [ArrayList].
 */
public class HitMarkList(
	private val elements: MutableList<net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark>,
) : MutableList<net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark> by elements {
    public constructor(capacity: Int = net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMarkList.Companion.DEFAULT_CAPACITY) : this(ArrayList(capacity))

    private companion object {
        private const val DEFAULT_CAPACITY = 10
    }
}
