package net.rsprot.protocol.api.js5

public class Js5Configuration public constructor(
    public val blockSizeInBytes: Int = 512,
    public val flushThresholdInBytes: Int = 10240,
    public val flushThresholdInRequests: Int = 10,
    public val priorityRatio: Int = 3,
) {
    init {
        require(blockSizeInBytes >= 8) {
            "Js5 block size must be at least 8 bytes"
        }
        require(priorityRatio >= 1) {
            "Priority ratio must be at least 1"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Js5Configuration

        if (blockSizeInBytes != other.blockSizeInBytes) return false
        if (flushThresholdInBytes != other.flushThresholdInBytes) return false
        if (flushThresholdInRequests != other.flushThresholdInRequests) return false
        if (priorityRatio != other.priorityRatio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockSizeInBytes
        result = 31 * result + flushThresholdInBytes
        result = 31 * result + flushThresholdInRequests
        result = 31 * result + priorityRatio
        return result
    }

    override fun toString(): String {
        return "Js5Configuration(" +
            "blockSizeInBytes=$blockSizeInBytes, " +
            "flushThresholdInBytes=$flushThresholdInBytes, " +
            "flushThresholdInRequests=$flushThresholdInRequests, " +
            "priorityRatio=$priorityRatio" +
            ")"
    }
}
