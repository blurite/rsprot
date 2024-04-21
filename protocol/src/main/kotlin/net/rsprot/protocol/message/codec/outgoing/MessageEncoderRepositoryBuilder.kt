package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.platform.Platform

public class MessageEncoderRepositoryBuilder<P : ServerProt, T : Platform>(
    private val platform: T,
    private val protRepository: ProtRepository<P>,
) {
    private val encoders: Array<MessageEncoder<*>?> = arrayOfNulls(protRepository.capacity())
    private val messageClassToServerProtMap: MutableMap<Class<out OutgoingMessage>, ServerProt> = hashMapOf()

    public inline fun <reified T : OutgoingMessage> bind(encoder: MessageEncoder<T>) {
        bind(T::class.java, encoder)
    }

    public fun <T : OutgoingMessage> bind(
        messageClass: Class<T>,
        encoder: MessageEncoder<*>,
    ) {
        val prot = encoder.prot
        require(encoders[prot.opcode] == null) {
            "Encoder for prot $prot is already bound."
        }
        encoders[prot.opcode] = encoder
        messageClassToServerProtMap[messageClass] = prot
    }

    public fun build(): MessageEncoderRepository<P, T> {
        return MessageEncoderRepository(
            platform,
            protRepository,
            encoders.copyOf(),
            messageClassToServerProtMap,
        )
    }
}
