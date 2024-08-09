package net.rsprot.protocol.message.codec.outgoing

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.MessageEncoder

public class MessageEncoderRepositoryBuilder<P : ServerProt>(
    private val protRepository: ProtRepository<P>,
) {
    private val encoders: Array<MessageEncoder<*>?> = arrayOfNulls(protRepository.capacity())
    private val messageClassToServerProtMap: MutableMap<Class<out OutgoingMessage>, ServerProt> = hashMapOf()

    public inline fun <reified T : OutgoingMessage> bind(encoder: MessageEncoder<T>) {
        bind(T::class.java, encoder)
    }

    public inline fun <reified T : OutgoingMessage> bindWithAlts(
        encoder: MessageEncoder<T>,
        vararg alternativeClasses: Class<out OutgoingMessage>,
    ) {
        bind(T::class.java, encoder)
        for (clazz in alternativeClasses) {
            bind(clazz, encoder, false)
        }
    }

    public fun <T : OutgoingMessage> bind(
        messageClass: Class<T>,
        encoder: MessageEncoder<*>,
        check: Boolean = true,
    ) {
        val prot = encoder.prot
        if (check) {
            require(encoders[prot.opcode] == null) {
                "Encoder for prot $prot is already bound."
            }
        }
        encoders[prot.opcode] = encoder
        messageClassToServerProtMap[messageClass] = prot
    }

    public fun build(): MessageEncoderRepository<P> =
        MessageEncoderRepository(
            protRepository,
            encoders.copyOf(),
            messageClassToServerProtMap,
        )
}
