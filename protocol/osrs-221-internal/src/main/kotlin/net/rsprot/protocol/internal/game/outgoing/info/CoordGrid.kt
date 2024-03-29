package net.rsprot.protocol.internal.game.outgoing.info

@Suppress("unused", "MemberVisibilityCanBePrivate")
@JvmInline
public value class CoordGrid(public val packed: Int) {
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    public constructor(
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

    public val level: Int
        get() = packed ushr 28
    public val x: Int
        get() = packed ushr 14 and 0x3FFF
    public val z: Int
        get() = packed and 0x3FFF

    public fun inDistance(
        other: CoordGrid,
        distance: Int,
    ): Boolean {
        if (level != other.level) {
            return false
        }
        val deltaX = x - other.x
        if (deltaX !in -distance..distance) {
            return false
        }
        val deltaZ = z - other.z
        return deltaZ in -distance..distance
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun invalid(): Boolean {
        return this == INVALID
    }

    public operator fun component1(): Int {
        return level
    }

    public operator fun component2(): Int {
        return x
    }

    public operator fun component3(): Int {
        return z
    }

    override fun toString(): String {
        return "CoordGrid(" +
            "level=$level, " +
            "x=$x, " +
            "z=$z" +
            ")"
    }

    public companion object {
        public val INVALID: CoordGrid = CoordGrid(-1)
    }
}
