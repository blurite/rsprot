package net.rsprot.protocol.game.outgoing.codec.inv

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvFull
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class UpdateInvFullEncoder : MessageEncoder<UpdateInvFull> {
    override val prot: ServerProt = GameServerProt.UPDATE_INV_FULL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateInvFull,
    ) {
        buffer.pCombinedId(message.combinedId)
        buffer.p2(message.inventoryId)
        val capacity = message.capacity
        buffer.p2(capacity)
        for (i in 0..<capacity) {
            val obj = message.getObject(i)
            if (obj == InventoryObject.NULL) {
                buffer.p1Alt2(0)
                buffer.p2(0)
                continue
            }
            val count = obj.count
            buffer.p1Alt2(count.coerceAtMost(0xFF))
            if (count >= 255) {
                buffer.p4Alt3(count)
            }
            buffer.p2(obj.id + 1)
        }
        message.returnInventory()
    }
}
