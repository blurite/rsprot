package net.rsprot.protocol.api.login

import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * The encoder for any login messages.
 */
public class LoginMessageEncoder(
    public val networkService: NetworkService<*>,
) : OutgoingMessageEncoder() {
    override val cipher: StreamCipher = NopStreamCipher
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.loginMessageEncoderRepository
    override val validate: Boolean = false
}
