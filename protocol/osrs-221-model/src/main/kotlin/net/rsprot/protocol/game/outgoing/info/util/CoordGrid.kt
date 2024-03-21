package net.rsprot.protocol.game.outgoing.info.util

@Suppress("unused", "MemberVisibilityCanBePrivate")
@JvmInline
internal value class CoordGrid(val packed: Int) {
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    constructor(
        level: Int,
        x: Int,
        z: Int,
    ) : this(
        (level shl 28)
            .or(x shl 14)
            .or(z),
    ) {
        // https://youtrack.jetbrains.com/issue/KT-62798/in-range-checks-are-not-intrinsified-in-kotlin-stdlib
        // Using traditional checks to avoid generating range objects (seen by decompiling this class)
        require(level >= 0 && level < 4) {
            "Level must be in range of 0..<4"
        }
        require(x >= 0 && x <= 16384) {
            "X coordinate must be in range of 0..<16384"
        }
        require(z >= 0 && z <= 16384) {
            "Y coordinate must be in range of 0..<16384"
        }
    }

    val level: Int
        get() = packed ushr 28
    val x: Int
        get() = packed ushr 14 and 0x3FFF
    val z: Int
        get() = packed and 0x3FFF

    @Suppress("NOTHING_TO_INLINE")
    inline fun invalid(): Boolean {
        return this == INVALID
    }

    operator fun component1(): Int {
        return level
    }

    operator fun component2(): Int {
        return x
    }

    operator fun component3(): Int {
        return z
    }

    override fun toString(): String {
        return "CoordGrid(" +
            "level=$level, " +
            "x=$x, " +
            "z=$z" +
            ")"
    }

    internal companion object {
        val INVALID: CoordGrid = CoordGrid(-1)
    }
}
