package net.rsprot.protocol.game.outgoing.zone.payload.util

@JvmInline
internal value class CoordInZone private constructor(
    val packed: UByte,
) {
    constructor(
        xInZone: Int,
        zInZone: Int,
    ) : this(
        (xInZone and 0x7 shl 4)
            .or(zInZone and 0x7)
            .toUByte(),
    )

    val xInZone: Int
        get() = packed.toInt() ushr 4 and 0x7
    val zInZone: Int
        get() = packed.toInt() and 0x7

    override fun toString(): String =
        "CoordInZone(" +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
}
