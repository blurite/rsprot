package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.TriggerOnDialogAbort
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class TriggerOnDialogAbortEncoder : MessageEncoder<TriggerOnDialogAbort> {
    override val prot: ServerProt = GameServerProt.TRIGGER_ONDIALOGABORT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: TriggerOnDialogAbort,
    ) {
    }
}
