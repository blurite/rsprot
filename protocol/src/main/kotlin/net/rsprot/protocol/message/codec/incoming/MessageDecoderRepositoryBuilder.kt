package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.codec.MessageDecoder

public class MessageDecoderRepositoryBuilder<P : ClientProt>(
    private val protRepository: ProtRepository<P>,
) {
    private val decoders: Array<MessageDecoder<*>?> = arrayOfNulls(protRepository.capacity())
    private val decoderClassToMessageClassMap:
        MutableMap<Class<out MessageDecoder<IncomingMessage>>, Class<out IncomingMessage>> = hashMapOf()

    public inline fun <reified T : IncomingMessage> bind(encoder: MessageDecoder<T>) {
        bind(T::class.java, encoder)
    }

    public fun <T : IncomingMessage> bind(
        messageClass: Class<T>,
        decoder: MessageDecoder<T>,
    ) {
        val clientProt = decoder.prot
        requireNotNull(decoders[clientProt.opcode] == null) {
            "Decoder for $messageClass is already bound."
        }
        decoders[clientProt.opcode] = decoder
        decoderClassToMessageClassMap[decoder::class.java] = messageClass
    }

    public fun build(): MessageDecoderRepository<P> {
        return MessageDecoderRepository(
            protRepository,
            decoders.copyOf(),
            decoderClassToMessageClassMap.toMap(),
        )
    }
}
