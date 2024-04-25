package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.codec.MessageDecoder

public class MessageDecoderRepository<out P : ClientProt> internal constructor(
    private val protRepository: ProtRepository<P>,
    private val decoders: Array<MessageDecoder<*>?>,
    private val decoderToMessageClassMap: Map<Class<out MessageDecoder<IncomingMessage>>, Class<out IncomingMessage>>,
) {
    public fun getDecoder(opcode: Int): MessageDecoder<*> {
        return decoders[opcode]
            ?: throw IllegalArgumentException("Opcode $opcode is not registered.")
    }

    public fun getMessageClass(decoderClazz: Class<out MessageDecoder<IncomingMessage>>): Class<out IncomingMessage> {
        return requireNotNull(decoderToMessageClassMap[decoderClazz]) {
            "Message class does not exist for $decoderClazz"
        }
    }

    public fun getSize(opcode: Int): Int {
        return protRepository.getSize(opcode)
    }
}
