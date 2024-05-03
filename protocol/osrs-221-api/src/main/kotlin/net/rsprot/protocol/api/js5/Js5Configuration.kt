package net.rsprot.protocol.api.js5

public class Js5Configuration public constructor(
    public val blockSize: Int = 512,
    public val flushThreshold: Int = 10240,
    public val priorityRatio: Int = 3,
) {
    init {
        require(blockSize >= 8) {
            "Js5 block size must be at least 8 bytes"
        }
        require(flushThreshold >= blockSize) {
            "Flush threshold must be at least the size of the block itself"
        }
        require(priorityRatio >= 1) {
            "Priority ratio must be at least 1"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Js5Configuration

        if (blockSize != other.blockSize) return false
        if (flushThreshold != other.flushThreshold) return false
        if (priorityRatio != other.priorityRatio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockSize
        result = 31 * result + flushThreshold
        result = 31 * result + priorityRatio
        return result
    }

    override fun toString(): String {
        return "Js5Configuration(" +
            "blockSize=$blockSize, " +
            "flushThreshold=$flushThreshold, " +
            "priorityRatio=$priorityRatio" +
            ")"
    }
}
