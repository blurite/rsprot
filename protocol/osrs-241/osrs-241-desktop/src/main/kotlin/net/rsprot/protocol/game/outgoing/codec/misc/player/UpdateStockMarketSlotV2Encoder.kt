package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlotV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateStockMarketSlotV2Encoder : MessageEncoder<UpdateStockMarketSlotV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_STOCKMARKET_SLOT_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateStockMarketSlotV2,
    ) {
        when (val update = message.update) {
            UpdateStockMarketSlotV2.ResetStockMarketSlot -> {
                // TODO(241): Verify the native reset branch's six unconsumed bytes.
                throw UnsupportedOperationException("Revision-241 stockmarket V2 reset is not verified")
            }
            is UpdateStockMarketSlotV2.SetStockMarketSlot -> {
                buffer.p1(message.slot)
                buffer.p1(7) // Escaped/versioned offer.
                buffer.p1(2)
                buffer.p1(update.status)
                buffer.p2(update.obj)
                buffer.p8(update.price)
                buffer.p4(update.count)
                buffer.p4(update.completedCount)
                buffer.p8(update.completedGold)
                buffer.p4(0) // No extension: the complete packet occupies exactly 34 bytes.
            }
        }
    }
}
