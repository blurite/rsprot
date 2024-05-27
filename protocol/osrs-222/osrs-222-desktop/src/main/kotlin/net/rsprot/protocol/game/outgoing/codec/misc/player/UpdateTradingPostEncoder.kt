package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateTradingPost
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateTradingPostEncoder : MessageEncoder<UpdateTradingPost> {
    override val prot: ServerProt = GameServerProt.UPDATE_TRADINGPOST

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateTradingPost,
    ) {
        when (val update = message.updateType) {
            UpdateTradingPost.ResetTradingPost -> {
                buffer.p1(0)
            }
            is UpdateTradingPost.SetTradingPostOfferList -> {
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
                    buffer.p4(offer.price)
                    buffer.p4(offer.count)
                }
            }
        }
    }
}
