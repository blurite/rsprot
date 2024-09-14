package net.rsprot.protocol.game.outgoing.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvFull
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class UpdateInvFullEncoder : MessageEncoder<UpdateInvFull> {
    override val prot: ServerProt = GameServerProt.UPDATE_INV_FULL

    override fun encode(
        streamCipher: StreamCipher,
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
                buffer.p2Alt3(0)
                continue
            }
            val count = InventoryObject.getCount(obj)
            buffer.p1Alt2(count.coerceAtMost(0xFF))
            if (count >= 255) {
                buffer.p4Alt2(count)
            }
            buffer.p2Alt3(InventoryObject.getId(obj) + 1)
        }
        message.returnInventory()
    }
}
