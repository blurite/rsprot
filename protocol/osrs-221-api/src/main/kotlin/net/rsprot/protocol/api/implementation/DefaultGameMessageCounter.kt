package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.api.GameMessageCounter
import net.rsprot.protocol.game.incoming.GameClientProtCategory

public class DefaultGameMessageCounter : GameMessageCounter {
    private val counts: IntArray = IntArray(PROT_TYPES_COUNT)

    override fun increment(clientProtCategory: ClientProtCategory) {
        counts[clientProtCategory.id]++
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
