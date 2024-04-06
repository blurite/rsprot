package net.rsprot.protocol.game.outgoing.zone.payload.util

@Suppress("MemberVisibilityCanBePrivate", "unused")
@JvmInline
internal value class LocProperties private constructor(
    val packed: UByte,
) {
    constructor(
        shape: Int,
        rotation: Int,
    ) : this(
        (shape and 0x1F shl 2)
            .or(rotation and 0x3)
            .toUByte(),
    )

    val shape: Int
        get() = packed.toInt() ushr 2 and 0x1F
    val rotation: Int
        get() = packed.toInt() and 0x3

    override fun toString(): String {
        return "LocProperties(" +
            "shape=$shape, " +
            "rotation=$rotation" +
            ")"
    }
}
