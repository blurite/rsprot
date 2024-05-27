package net.rsprot.protocol.game.outgoing.codec.inv

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.inv.UpdateInvStopTransmit
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateInvStopTransmitEncoder : MessageEncoder<UpdateInvStopTransmit> {
    override val prot: ServerProt = GameServerProt.UPDATE_INV_STOPTRANSMIT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateInvStopTransmit,
    ) {
        buffer.p2Alt3(message.inventoryId)
    }
}
