package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.platform.Platform

public class MessageEncoderRepositoryBuilder<P : ServerProt, T : Platform>(
    private val platform: T,
    private val protRepository: ProtRepository<P>,
) {
    private val encoders: Array<MessageEncoder<*>?> = arrayOfNulls(protRepository.capacity())

    public fun bind(encoder: MessageEncoder<*>) {
        val prot = encoder.prot
        require(encoders[prot.opcode] == null) {
            "Encoder for prot $prot is already bound."
        }
        encoders[prot.opcode] = encoder
    }

    public fun build(): MessageEncoderRepository<P, T> {
        return MessageEncoderRepository(
            platform,
            protRepository,
            encoders.copyOf(),
        )
    }
}
