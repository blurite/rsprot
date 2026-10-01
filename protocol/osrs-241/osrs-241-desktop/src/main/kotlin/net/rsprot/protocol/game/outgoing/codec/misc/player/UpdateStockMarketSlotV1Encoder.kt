package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlotV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateStockMarketSlotV1Encoder : MessageEncoder<UpdateStockMarketSlotV1> {
    override val prot: ServerProt = GameServerProt.UPDATE_STOCKMARKET_SLOT_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStockMarketSlotV1,
    ) {
        buffer.p1(message.slot)
        when (val update = message.update) {
            UpdateStockMarketSlotV1.ResetStockMarketSlot -> {
                buffer.p1(0)
                buffer.skipWrite(18)
            }
            is UpdateStockMarketSlotV1.SetStockMarketSlot -> {
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
