package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.platform.Platform

public class MessageEncoderRepository<P : ServerProt, T : Platform> internal constructor(
    @Suppress("unused") private val platform: T,
    private val protRepository: ProtRepository<P>,
    private val encoders: Array<MessageEncoder<*>?>,
    private val messageClassToServerProtMap: Map<Class<out OutgoingMessage>, ServerProt>,
) {
    private fun getEncoder(opcode: Int): MessageEncoder<*> {
        return encoders[opcode]
            ?: throw IllegalArgumentException("Opcode $opcode is not registered.")
    }

    public fun <Type : OutgoingMessage> getEncoder(clazz: Class<out Type>): MessageEncoder<Type> {
        val prot = getServerProt(clazz)
        @Suppress("UNCHECKED_CAST")
        return getEncoder(prot.opcode) as MessageEncoder<Type>
    }

    private fun getServerProt(clazz: Class<out OutgoingMessage>): ServerProt {
        val serverProt = messageClassToServerProtMap[clazz]
        requireNotNull(serverProt) {
            "Encoder not registered for $clazz."
        }
        return serverProt
    }

    public fun getSize(opcode: Int): Int {
        return protRepository.getSize(opcode)
    }
}
