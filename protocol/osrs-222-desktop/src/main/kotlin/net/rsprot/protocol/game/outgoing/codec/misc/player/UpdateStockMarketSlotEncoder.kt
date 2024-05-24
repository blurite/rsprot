package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateStockMarketSlotEncoder : MessageEncoder<UpdateStockMarketSlot> {
    override val prot: ServerProt = GameServerProt.UPDATE_STOCKMARKET_SLOT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateStockMarketSlot,
    ) {
        buffer.p1(message.slot)
        when (val update = message.update) {
            UpdateStockMarketSlot.ResetStockMarketSlot -> {
                buffer.p1(0)
                buffer.skipWrite(18)
            }
            is UpdateStockMarketSlot.SetStockMarketSlot -> {
                buffer.p1(update.status)
                buffer.p2(update.obj)
                buffer.p4(update.price)
                buffer.p4(update.count)
                buffer.p4(update.completedCount)
                buffer.p4(update.completedGold)
            }
        }
    }
}
