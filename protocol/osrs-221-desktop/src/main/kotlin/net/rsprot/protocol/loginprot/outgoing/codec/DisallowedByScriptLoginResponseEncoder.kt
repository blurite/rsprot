package net.rsprot.protocol.loginprot.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class DisallowedByScriptLoginResponseEncoder : MessageEncoder<LoginResponse.DisallowedByScript> {
    override val prot: ServerProt = LoginServerProt.DISALLOWED_BY_SCRIPT

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LoginResponse.DisallowedByScript,
    ): JagByteBuf {
        buffer.pjstr(message.line1)
        buffer.pjstr(message.line2)
        buffer.pjstr(message.line3)
        return buffer
    }
}
