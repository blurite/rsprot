package net.rsprot.protocol.common.loginprot.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class SuccessfulLoginResponseEncoder : MessageEncoder<LoginResponse.Successful> {
    override val prot: ServerProt = LoginServerProt.SUCCESSFUL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LoginResponse.Successful,
    ) {
        val sessionId = message.sessionId ?: return
        buffer.p8(sessionId)
    }
}
