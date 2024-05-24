package net.rsprot.protocol.game.outgoing.codec.logout

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.logout.LogoutWithReason
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class LogoutWithReasonEncoder : MessageEncoder<LogoutWithReason> {
    override val prot: ServerProt = GameServerProt.LOGOUT_WITHREASON

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LogoutWithReason,
    ) {
        buffer.p1(message.reason)
    }
}
