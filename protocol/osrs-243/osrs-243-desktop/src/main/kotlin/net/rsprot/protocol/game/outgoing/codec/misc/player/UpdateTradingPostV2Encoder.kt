package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateTradingPostV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateTradingPostV2Encoder : MessageEncoder<UpdateTradingPostV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_TRADINGPOST_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateTradingPostV2,
    ) {
        when (val update = message.updateType) {
            UpdateTradingPostV2.ResetTradingPost -> {
                buffer.p1(0)
            }
            is UpdateTradingPostV2.SetTradingPostOfferList -> {
                buffer.p1(1)
                buffer.p8(update.age)
                buffer.p2(update.obj)
                buffer.p1(if (update.status) 1 else 0)
                val offers = update.offers
                buffer.p2(offers.size)
                for (offer in offers) {
                    buffer.pjstr(offer.name)
                    buffer.pjstr(offer.previousName)
                    buffer.p2(offer.world)
                    buffer.p8(offer.time)
                    buffer.p8(offer.price)
                    buffer.p4(offer.count)
                }
            }
        }
    }
}
