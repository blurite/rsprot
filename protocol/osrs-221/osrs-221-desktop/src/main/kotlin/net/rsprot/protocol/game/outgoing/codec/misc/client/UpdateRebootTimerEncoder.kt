package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.UpdateRebootTimer
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateRebootTimerEncoder : MessageEncoder<UpdateRebootTimer> {
    override val prot: ServerProt = GameServerProt.UPDATE_REBOOT_TIMER

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateRebootTimer,
    ) {
        buffer.p2Alt1(message.gameCycles)
    }
}
