package net.rsprot.protocol.loginprot.outgoing.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.outgoing.util.AuthenticatorResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class OkLoginResponseEncoder : MessageEncoder<LoginResponse.Ok> {
    override val prot: ServerProt = LoginServerProt.OK

    override fun encode(
        buffer: JagByteBuf,
        message: LoginResponse.Ok,
    ) {
        when (val response = message.authenticatorResponse) {
            is AuthenticatorResponse.AuthenticatorCode -> {
                buffer.p1(1)
                // TODO: Each byte must be ISAAC encrypted
                buffer.p4(response.code)
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
    }
}
