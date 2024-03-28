package net.rsprot.protocol.game.outgoing.info.playerinfo.util

import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

@JvmInline
internal value class LowResolutionPosition(val packed: Int) {
    val x: Int
        get() = packed ushr 8 and 0xFF
    val z: Int
        get() = packed and 0xFF
    val level: Int
        get() = packed ushr 16 and 0x3
}

internal fun LowResolutionPosition(coordGrid: CoordGrid): LowResolutionPosition {
    return LowResolutionPosition(
        (coordGrid.z ushr 13)
            .or((coordGrid.x ushr 13) shl 8)
            .or((coordGrid.level shl 16)),
    )
}
