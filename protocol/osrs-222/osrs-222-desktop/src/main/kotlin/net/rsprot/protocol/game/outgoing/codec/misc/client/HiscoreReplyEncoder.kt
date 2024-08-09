package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.HiscoreReply
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class HiscoreReplyEncoder : MessageEncoder<HiscoreReply> {
    override val prot: ServerProt = GameServerProt.HISCORE_REPLY

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: HiscoreReply,
    ) {
        buffer.p1(message.requestId)
        when (val response = message.response) {
            is HiscoreReply.FailedHiscoreReply -> {
                buffer.p1(1)
                buffer.pjstr(response.reason)
            }
            is HiscoreReply.SuccessfulHiscoreReply -> {
                buffer.p1(0)
                buffer.p1(response.statResults.size)
                for (stat in response.statResults) {
                    buffer.p2(stat.id)
                    buffer.p4(stat.rank)
                    buffer.p4(stat.result)
                }
                buffer.p4(response.overallRank)
                buffer.p8(response.overallExperience)
                buffer.p2(response.activityResults.size)
                for (activity in response.activityResults) {
                    buffer.p2(activity.id)
                    buffer.p4(activity.rank)
                    buffer.p4(activity.result)
                }
            }
        }
    }
}
