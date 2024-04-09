package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.platform.Platform

public class MessageEncoderRepository<P : ServerProt, T : Platform> internal constructor(
    @Suppress("unused") private val platform: T,
    private val protRepository: ProtRepository<P>,
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
