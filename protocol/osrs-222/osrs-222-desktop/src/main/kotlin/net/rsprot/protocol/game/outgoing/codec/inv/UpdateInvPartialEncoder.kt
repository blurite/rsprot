package net.rsprot.protocol.game.outgoing.codec.inv

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.pCombinedId

@Consistent
public class UpdateInvPartialEncoder : MessageEncoder<UpdateInvPartial> {
    override val prot: ServerProt = GameServerProt.UPDATE_INV_PARTIAL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateInvPartial,
    ) {
        buffer.pCombinedId(message.combinedId)
        buffer.p2(message.inventoryId)
        for (i in 0..<message.count) {
            val obj = message.getObject(i)
            buffer.pSmart1or2(obj.slot)
            val id = obj.id
            if (id == -1) {
                buffer.p2(0)
                continue
            }
            buffer.p2(id + 1)
            val count = obj.count
            buffer.p1(count.coerceAtMost(0xFF))
            if (count >= 0xFF) {
                buffer.p4(count)
            }
        }
        message.returnInventory()
    }
}
