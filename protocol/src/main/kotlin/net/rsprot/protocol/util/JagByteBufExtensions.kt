package net.rsprot.protocol.util

import net.rsprot.buffer.JagByteBuf

public fun JagByteBuf.gCombinedId(): CombinedId = CombinedId(g4())

public fun JagByteBuf.gCombinedIdAlt1(): CombinedId = CombinedId(g4Alt1())

public fun JagByteBuf.gCombinedIdAlt2(): CombinedId = CombinedId(g4Alt2())

public fun JagByteBuf.gCombinedIdAlt3(): CombinedId = CombinedId(g4Alt3())

@JvmSynthetic
@JvmName("pCombinedIdPacked")
public fun JagByteBuf.pCombinedId(combinedId: CombinedId): JagByteBuf = p4(combinedId.combinedId)

@JvmSynthetic
@JvmName("pCombinedIdAlt1Packed")
public fun JagByteBuf.pCombinedIdAlt1(combinedId: CombinedId): JagByteBuf = p4Alt1(combinedId.combinedId)

@JvmSynthetic
@JvmName("pCombinedIdAlt2Packed")
public fun JagByteBuf.pCombinedIdAlt2(combinedId: CombinedId): JagByteBuf = p4Alt2(combinedId.combinedId)

@JvmSynthetic
@JvmName("pCombinedIdAlt3Packed")
public fun JagByteBuf.pCombinedIdAlt3(combinedId: CombinedId): JagByteBuf = p4Alt3(combinedId.combinedId)

public fun JagByteBuf.pCombinedId(combinedId: Int): JagByteBuf = p4(combinedId)

public fun JagByteBuf.pCombinedIdAlt1(combinedId: Int): JagByteBuf = p4Alt1(combinedId)

public fun JagByteBuf.pCombinedIdAlt2(combinedId: Int): JagByteBuf = p4Alt2(combinedId)

public fun JagByteBuf.pCombinedIdAlt3(combinedId: Int): JagByteBuf = p4Alt3(combinedId)
