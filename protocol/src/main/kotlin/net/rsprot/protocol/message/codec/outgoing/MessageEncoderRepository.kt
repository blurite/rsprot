package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.codec.MessageEncoder

public class MessageEncoderRepository internal constructor(
    private val protRepository: ProtRepository,
    private val encoders: Array<MessageEncoder<*>?>,
) {
    public fun getEncoder(opcode: Int): MessageEncoder<*> {
        return encoders[opcode]
            ?: throw IllegalArgumentException("Opcode $opcode is not registered.")
    }

    public fun getSize(opcode: Int): Int {
        return protRepository.getSize(opcode)
    }
}
