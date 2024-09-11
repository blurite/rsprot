package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Hiscore reply is a packet used in the enhanced clients to do
 * lookups of nearby players, to find out their stats and rankings
 * on the high scores.
 * This packet is sent as a response to the hiscore request packet.
 * @property requestId the id of the request that was made.
 * @property response the response to be written to the client.
 */
public class HiscoreReply private constructor(
    private val _requestId: UByte,
    public val response: HiscoreReplyResponse,
) : OutgoingGameMessage {
    public constructor(
        requestId: Int,
        response: HiscoreReplyResponse,
    ) : this(
        requestId.toUByte(),
        response,
    )

    public val requestId: Int
        get() = _requestId.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HiscoreReply

        if (_requestId != other._requestId) return false
        if (response != other.response) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _requestId.hashCode()
        result = 31 * result + response.hashCode()
        return result
    }

    override fun toString(): String =
        "HiscoreReply(" +
            "requestId=$requestId, " +
            "response=$response" +
            ")"

    public sealed interface HiscoreReplyResponse

    /**
     * A successful hiscore reply, transmitting all the stat and activity results.
     * It is worth noting that because the packet isn't used, it is not entirely
     * certain that the naming of these properties is accurate. These are merely
     * a guess based on the hiscore json syntax.
     * @property statResults the list of stats to transmit
     * @property overallRank the overall rank of this player based on the total level
     * @property overallExperience the overall experience of this player
     * @property activityResults the list of activity results to transmit.
     */
    public class SuccessfulHiscoreReply(
        public val statResults: List<HiscoreResult>,
        public val overallRank: Int,
        public val overallExperience: Long,
        public val activityResults: List<HiscoreResult>,
    ) : HiscoreReplyResponse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SuccessfulHiscoreReply

            if (statResults != other.statResults) return false
            if (overallRank != other.overallRank) return false
            if (overallExperience != other.overallExperience) return false
            if (activityResults != other.activityResults) return false

            return true
        }

        override fun hashCode(): Int {
            var result = statResults.hashCode()
            result = 31 * result + overallRank
            result = 31 * result + overallExperience.hashCode()
            result = 31 * result + activityResults.hashCode()
            return result
        }

        override fun toString(): String =
            "SuccessfulHiscoreReply(" +
                "statResults=$statResults, " +
                "overallRank=$overallRank, " +
                "overallExperience=$overallExperience, " +
                "activityResults=$activityResults" +
                ")"
    }

    /**
     * A failed hiscore reply would be sent when a lookup could not be
     * performed successfully. The client will read a string for a reason
     * when this occurs.
     * @property reason the reason to give to the player for why
     * the lookup could not be done.
     */
    public class FailedHiscoreReply(
        public val reason: String,
    ) : HiscoreReplyResponse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FailedHiscoreReply

            return reason == other.reason
        }

        override fun hashCode(): Int = reason.hashCode()

        override fun toString(): String = "FailedHiscoreReply(reason='$reason')"
    }

    public class HiscoreResult(
        public val id: Int,
        public val rank: Int,
        public val result: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HiscoreResult

            if (id != other.id) return false
            if (rank != other.rank) return false
            if (result != other.result) return false

            return true
        }

        override fun hashCode(): Int {
            var result1 = id
            result1 = 31 * result1 + rank
            result1 = 31 * result1 + result
            return result1
        }

        override fun toString(): String =
            "HiscoreResult(" +
                "id=$id, " +
                "rank=$rank, " +
                "result=$result" +
                ")"
    }
}
