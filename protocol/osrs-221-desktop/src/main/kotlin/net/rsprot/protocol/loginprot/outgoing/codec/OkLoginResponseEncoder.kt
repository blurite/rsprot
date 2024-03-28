package net.rsprot.protocol.loginprot.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.outgoing.util.AuthenticatorResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class OkLoginResponseEncoder : MessageEncoder<LoginResponse.Ok> {
    override val prot: ServerProt = LoginServerProt.OK

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LoginResponse.Ok,
    ): JagByteBuf {
        when (val response = message.authenticatorResponse) {
            is AuthenticatorResponse.AuthenticatorCode -> {
                val cipherPair =
                    ctx.channel().attr(ChannelAttributes.STREAM_CIPHER_PAIR).get()
                        ?: throw IllegalStateException("Stream cipher not initialized.")
                val encoderCipher = cipherPair.encoderCipher
                val code = response.code
                buffer.p1(1)
                buffer.p1((code ushr 24 and 0xFF) + encoderCipher.nextInt())
                buffer.p1((code ushr 16 and 0xFF) + encoderCipher.nextInt())
                buffer.p1((code ushr 8 and 0xFF) + encoderCipher.nextInt())
                buffer.p1((code and 0xFF) + encoderCipher.nextInt())
            }
            AuthenticatorResponse.NoAuthenticator -> {
                buffer.p1(0)
                buffer.p4(0)
            }
        }
        buffer.p1(message.staffModLevel)
        buffer.pboolean(message.playerMod)
        buffer.p2(message.index)
        buffer.pboolean(message.member)
        buffer.p8(message.accountHash)
        buffer.p8(message.userId)
        buffer.p8(message.userHash)
        return buffer
    }
}
