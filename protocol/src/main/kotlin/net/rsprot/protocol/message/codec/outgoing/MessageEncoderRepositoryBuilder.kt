package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.codec.MessageEncoder

public class MessageEncoderRepositoryBuilder(
    private val protRepository: ProtRepository,
) {
    private val encoders: Array<MessageEncoder<*>?> = arrayOfNulls(protRepository.capacity())

    public fun bind(encoder: MessageEncoder<*>) {
        val prot = encoder.prot
        require(encoders[prot.opcode] == null) {
            "Encoder for prot $prot is already bound."
        }
        encoders[prot.opcode] = encoder
    }

    public fun build(): MessageEncoderRepository {
        return MessageEncoderRepository(
            protRepository,
            encoders.copyOf(),
        )
    }
}
