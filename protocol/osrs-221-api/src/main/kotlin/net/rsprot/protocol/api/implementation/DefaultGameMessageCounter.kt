package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.ProtCategory
import net.rsprot.protocol.api.GameMessageCounter
import net.rsprot.protocol.game.incoming.GameClientProtCategory

public class DefaultGameMessageCounter : GameMessageCounter {
    private val counts: IntArray = IntArray(PROT_TYPES_COUNT)

    override fun increment(protCategory: ProtCategory) {
        counts[protCategory.id]++
    }

    override fun isFull(): Boolean {
        return GameClientProtCategory.entries.any { entry ->
            counts[entry.id] >= entry.limit
        }
    }

    override fun reset() {
        counts.fill(0)
    }

    private companion object {
        private const val PROT_TYPES_COUNT: Int = 2
    }
}
