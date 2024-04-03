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
