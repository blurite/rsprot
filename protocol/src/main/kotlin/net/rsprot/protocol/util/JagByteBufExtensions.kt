package net.rsprot.protocol.util

import net.rsprot.buffer.JagByteBuf

public fun JagByteBuf.gCombinedId(): CombinedId {
    return CombinedId(g4())
}

public fun JagByteBuf.gCombinedIdAlt1(): CombinedId {
    return CombinedId(g4Alt1())
}

public fun JagByteBuf.gCombinedIdAlt2(): CombinedId {
    return CombinedId(g4Alt2())
}

public fun JagByteBuf.gCombinedIdAlt3(): CombinedId {
    return CombinedId(g4Alt3())
}

public fun JagByteBuf.pCombinedId(combinedId: CombinedId): JagByteBuf {
    return p4(combinedId.combinedId)
}

public fun JagByteBuf.pCombinedIdAlt1(combinedId: CombinedId): JagByteBuf {
    return p4Alt1(combinedId.combinedId)
}

public fun JagByteBuf.pCombinedIdAlt2(combinedId: CombinedId): JagByteBuf {
    return p4Alt2(combinedId.combinedId)
}

public fun JagByteBuf.pCombinedIdAlt3(combinedId: CombinedId): JagByteBuf {
    return p4Alt3(combinedId.combinedId)
}
