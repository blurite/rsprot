package net.rsprot.protocol.game.outgoing.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.inv.UpdateInvStopTransmit
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateInvStopTransmitEncoder : MessageEncoder<UpdateInvStopTransmit> {
    override val prot: ServerProt = GameServerProt.UPDATE_INV_STOPTRANSMIT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateInvStopTransmit,
    ) {
        buffer.p2(message.inventoryId)
    }
}
