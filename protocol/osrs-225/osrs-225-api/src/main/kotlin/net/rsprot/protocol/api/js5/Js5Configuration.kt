package net.rsprot.protocol.api.js5

/**
 * The configuration for the JS5 service per client basis.
 * @property blockSizeInBytes the maximum number of bytes written per client per iteration
 * @property flushThresholdInBytes the minimum number of bytes that must be written into
 * the channel before a flush occurs. Note that the flush will only occur if at least one
 * group has been successfully written over, as there's no point in flushing a partial
 * large group - the client cannot continue anyhow. Furthermore, flushing occurs if
 * there's no more data to write, ignoring both the [flushThresholdInBytes] and
 * [flushThresholdInRequests] thresholds in the process.
 * @property flushThresholdInRequests the number of full requests that must be written
 * into this channel since the last flush before another flush can trigger. As explained
 * before, a flush will take place if no more data can be written out, ignoring this threshold.
 * @property priorityRatio the ratio for how much more data to write to logged in clients
 * compared to those logged out. A ratio of 3 means that per iteration, any low priority
 * client will receive [blockSizeInBytes] number of bytes, however any logged in client
 * will receive [blockSizeInBytes] * 3 number of bytes. This effectively gives priority to
 * those logged in, as they are in more of a need for data than anyone sitting on the loading
 * screen.
 * @property prefetchTransferThresholdInBytes the number of bytes to transfer from the 'pending'
 * prefetch collection over to the 'being served' prefetch collection. This is a soft cap,
 * meaning it will keep transferring groups until the moment that the sum of all transferred
 * is equal to or above this value. It is worth noting that this will be uncapped when
 * the user reaches the login screen, allowing for fast downloads of the cache if one
 * chooses to sit on the login screen. Before reaching the login screen, however, the
 * thresholds are still applied.
 * Below are some numbers of how long it takes to transfer
 * the entire OldSchool cache over via localhost using various thresholds:
 * Uncapped - 1 minute, 20 seconds
 * 16,384 bytes: 3 minutes, 30 seconds
 * 8,192 bytes: 5 minutes, 52 seconds
 * 4,096 bytes: 15 minutes, 26 seconds
 *
 * It is worth noting that prefetch groups are NOT necessary, one could disable them altogether,
 * however this would mean the users will experience small loading screens even days into the gameplay.
 * Sweet spot is having as small delays as possible when the client requires urgent responses, but
 * still downloading the entire thing as soon as possible. 8,192 bytes appears to be that sweet spot.
 */
public class Js5Configuration public constructor(
    public val blockSizeInBytes: Int = 512,
    public val flushThresholdInBytes: Int = 10240,
    public val flushThresholdInRequests: Int = 10,
    public val priorityRatio: Int = 3,
    public val prefetchTransferThresholdInBytes: Int = 8192,
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
        if (prefetchTransferThresholdInBytes != other.prefetchTransferThresholdInBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockSizeInBytes
        result = 31 * result + flushThresholdInBytes
        result = 31 * result + flushThresholdInRequests
        result = 31 * result + priorityRatio
        result = 31 * result + prefetchTransferThresholdInBytes
        return result
    }

    override fun toString(): String =
        "Js5Configuration(" +
            "blockSizeInBytes=$blockSizeInBytes, " +
            "flushThresholdInBytes=$flushThresholdInBytes, " +
            "flushThresholdInRequests=$flushThresholdInRequests, " +
            "priorityRatio=$priorityRatio, " +
            "prefetchTransferThresholdInBytes=$prefetchTransferThresholdInBytes" +
            ")"
}
