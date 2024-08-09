package net.rsprot.protocol.api.js5

import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.decoder.IncomingMessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A message decoder for JS5 packets.
 */
public class Js5MessageDecoder(
    public val networkService: NetworkService<*>,
) : IncomingMessageDecoder() {
    override val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .js5MessageDecoderRepository
    override val streamCipher: StreamCipher = NopStreamCipher
}
