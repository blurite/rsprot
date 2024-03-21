package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.codec.MessageDecoder

@Suppress("MemberVisibilityCanBePrivate")
public class MessageDecoderRepositoryBuilder<P : ClientProt>(
    private val protRepository: ProtRepository<P>,
) {
    private val decoders: Array<MessageDecoder<*>?> = arrayOfNulls(protRepository.capacity())
    private val messageClassToClientProtMap: MutableMap<Class<out IncomingMessage>, ClientProt> = hashMapOf()

    public fun <T : IncomingMessage> bind(
        messageClass: Class<T>,
        decoder: MessageDecoder<T>,
    ) {
        val clientProt = decoder.prot
        requireNotNull(decoders[clientProt.opcode] == null) {
            "Decoder for $messageClass is already bound."
        }
        decoders[clientProt.opcode] = decoder
        messageClassToClientProtMap[messageClass] = clientProt
    }

    public fun build(): MessageDecoderRepository<P> {
        return MessageDecoderRepository(
            protRepository,
            decoders.copyOf(),
            messageClassToClientProtMap.toMap(),
        )
    }
}
