package net.rsprot.protocol.game.outgoing.info.playerinfo.util

import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

/**
 * A value class for holding low resolution position information in a primitive int.
 * @param packed the bitpacked representation of the low resolution position
 */
@JvmInline
internal value class LowResolutionPosition(val packed: Int) {
    val x: Int
        get() = packed ushr 8 and 0xFF
    val z: Int
        get() = packed and 0xFF
    val level: Int
        get() = packed ushr 16 and 0x3
}

/**
 * A fake constructor for the low resolution position value class, as the JVM signature
 * matches that of the primary constructor.
 * @param coordGrid the absolute coordinate to turn into a low resolution position.
 * @return the low resolution representation of the given [coordGrid]
 */
internal fun LowResolutionPosition(coordGrid: CoordGrid): LowResolutionPosition {
    return LowResolutionPosition(
        (coordGrid.z ushr 13)
            .or((coordGrid.x ushr 13) shl 8)
            .or((coordGrid.level shl 16)),
    )
}
