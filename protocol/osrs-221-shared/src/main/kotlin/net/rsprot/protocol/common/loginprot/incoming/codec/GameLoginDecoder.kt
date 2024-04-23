package net.rsprot.protocol.common.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.LoginBlockDecoder
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.loginprot.incoming.GameLogin
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.OtpAuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.Password
import net.rsprot.protocol.loginprot.incoming.util.Token
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import java.math.BigInteger

public class GameLoginDecoder(
    exp: BigInteger,
    mod: BigInteger,
) : MessageDecoder<GameLogin>, LoginBlockDecoder<AuthenticationType<*>>(exp, mod) {
    override val prot: ClientProt = LoginClientProt.GAMELOGIN

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): GameLogin {
        return GameLogin(decodeLoginBlock(buffer))
    }

    override fun decodeAuthentication(buffer: JagByteBuf): AuthenticationType<*> {
        val otp = decodeOtpAuthentication(buffer)
        return when (val authenticationType = buffer.g1()) {
            PASSWORD_AUTHENTICATION ->
                AuthenticationType.PasswordAuthentication(
                    Password(buffer.gjstr().toByteArray()),
                    otp,
                )
            TOKEN_AUTHENTICATION ->
                AuthenticationType.TokenAuthentication(
                    Token(buffer.gjstr().toByteArray()),
                    otp,
                )
            else -> {
                throw IllegalStateException("Unknown authentication type: $authenticationType")
            }
        }
    }

    private fun decodeOtpAuthentication(buffer: JagByteBuf): OtpAuthenticationType {
        return when (val otpType = buffer.g1()) {
            OTP_TOKEN -> {
                val identifier = buffer.g4()
                buffer.skipWrite(1)
                OtpAuthenticationType.TrustedComputer(identifier)
            }
            OTP_REMEMBER -> {
                val otpKey = buffer.g3()
                buffer.skipWrite(1)
                OtpAuthenticationType.TrustedAuthenticator(otpKey)
            }
            OTP_NONE -> {
                buffer.skipWrite(4)
                OtpAuthenticationType.NoMultiFactorAuthentication
            }
            OTP_FORGET -> {
                val otpKey = buffer.g3()
                buffer.skipWrite(1)
                OtpAuthenticationType.UntrustedAuthentication(otpKey)
            }
            else -> {
                throw IllegalStateException("Unknown authentication type: $otpType")
            }
        }
    }

    private companion object {
        private const val OTP_TOKEN = 0
        private const val OTP_REMEMBER = 1
        private const val OTP_NONE = 2
        private const val OTP_FORGET = 3

        private const val PASSWORD_AUTHENTICATION = 0
        private const val TOKEN_AUTHENTICATION = 2
    }
}
