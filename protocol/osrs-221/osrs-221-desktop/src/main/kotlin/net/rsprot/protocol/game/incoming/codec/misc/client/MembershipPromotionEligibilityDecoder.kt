package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.MembershipPromotionEligibility
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MembershipPromotionEligibilityDecoder : MessageDecoder<MembershipPromotionEligibility> {
    override val prot: ClientProt = GameClientProt.MEMBERSHIP_PROMOTION_ELIGIBILITY

    override fun decode(buffer: JagByteBuf): MembershipPromotionEligibility {
        val eligibleForIntroductoryPrice = buffer.g1()
        val eligibleForTrialPurchase = buffer.g1()
        return MembershipPromotionEligibility(
            eligibleForIntroductoryPrice,
            eligibleForTrialPurchase,
        )
    }
}
