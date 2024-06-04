package net.rsprot.protocol.common.loginprot.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class ReconnectOkResponseEncoder : MessageEncoder<LoginResponse.ReconnectOk> {
    override val prot: ServerProt = LoginServerProt.RECONNECT_OK

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LoginResponse.ReconnectOk,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
