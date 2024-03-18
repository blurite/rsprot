package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.codec.MessageDecoder

public class MessageDecoderRepository internal constructor(
    private val protRepository: ProtRepository,
    private val decoders: Array<MessageDecoder<*>?>,
    private val messageClassToClientProtMap: Map<Class<out IncomingMessage>, ClientProt>,
) {
    public fun getDecoder(opcode: Int): MessageDecoder<*> {
        return decoders[opcode]
            ?: throw IllegalArgumentException("Opcode $opcode is not registered.")
    }

    public fun getClientProt(clazz: Class<out IncomingMessage>): ClientProt {
        val clientProt = messageClassToClientProtMap[clazz]
        requireNotNull(clientProt) {
            "Decoder not registered for $clazz."
        }
        return clientProt
    }

    public fun getSize(opcode: Int): Int {
        return protRepository.getSize(opcode)
    }

    public fun <T> toIncomingMessageRepositoryBuilder(): IncomingMessageRepositoryBuilder<T> {
        return IncomingMessageRepositoryBuilder(
            protRepository,
            decoders,
            messageClassToClientProtMap,
        )
    }
}
