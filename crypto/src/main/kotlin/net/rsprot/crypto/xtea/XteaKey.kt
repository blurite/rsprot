package net.rsprot.crypto.xtea

public class XteaKey(
    public val key: IntArray,
) {
    public constructor(
        key1: Int,
        key2: Int,
        key3: Int,
        key4: Int,
    ) : this(
        intArrayOf(
            key1,
            key2,
            key3,
            key4,
        ),
    )

    init {
        require(key.size == 4) {
            "Xtea keys must be 128 bits in length (4 integers)"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XteaKey

        return key.contentEquals(other.key)
    }

    override fun hashCode(): Int {
        return key.contentHashCode()
    }

    override fun toString(): String {
        return "XteaKey(key=${key.contentToString()})"
    }

    public companion object {
        public val ZERO: XteaKey = XteaKey(0, 0, 0, 0)
    }
}
