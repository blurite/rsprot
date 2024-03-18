package net.rsprot.protocol.client.incoming

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import java.util.function.BiConsumer

public class IncomingMessageRepositoryBuilder<R> internal constructor(
    private val protRepository: ProtRepository,
    private val decoders: Array<MessageDecoder<*>?>,
    private val messageClassToClientProtMap: Map<Class<out IncomingMessage>, ClientProt>,
) {
    private val consumers: Array<BiConsumer<R, out IncomingMessage>?> = arrayOfNulls(protRepository.capacity())

    public fun <T : IncomingMessage> addListener(
        clazz: Class<out T>,
        consumer: BiConsumer<R, out T>,
    ) {
        val clientProt = messageClassToClientProtMap[clazz]
        requireNotNull(clientProt) {
            "Decoder not registered for $clazz."
        }
        require(consumers[clientProt.opcode] == null) {
            "Consumer for $clazz is already bound."
        }
        consumers[clientProt.opcode] = consumer
    }

    @JvmSynthetic
    public inline fun <reified T : IncomingMessage> addListener(crossinline listener: R.(message: T) -> Unit) {
        addListener(T::class.java) { r, t -> listener(r, t) }
    }

    public fun build(): IncomingMessageRepository<R> {
        return IncomingMessageRepository(
            protRepository,
            decoders,
            consumers.copyOf(),
        )
    }
}
