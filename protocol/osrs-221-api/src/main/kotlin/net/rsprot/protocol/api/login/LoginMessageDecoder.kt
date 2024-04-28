package net.rsprot.protocol.api.login

import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.decoder.IncomingMessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.tools.MessageDecodingTools

public class LoginMessageDecoder(
    public val networkService: NetworkService<*, *>,
) : IncomingMessageDecoder() {
    override val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .loginMessageDecoderRepository
    override val messageDecodingTools: MessageDecodingTools = networkService.messageDecodingTools
    override val streamCipher: StreamCipher = NopStreamCipher
}
