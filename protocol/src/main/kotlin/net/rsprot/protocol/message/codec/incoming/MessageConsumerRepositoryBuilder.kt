package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingMessage
import java.util.function.BiConsumer

/**
 * Message consumer repository is a repository of listeners that the server will register
 * for various incoming messages. As there can be multiple clients, these cannot directly
 * be stored with the respective decoders.
 *
 * @property consumers the hashmap of consumers, using incoming message classes as keys.
 */
public abstract class MessageConsumerRepositoryBuilder<R> {
    private val consumers: MutableMap<Class<out IncomingMessage>, BiConsumer<R, in IncomingMessage>> = HashMap()

    /**
     * Adds a listener for the provided [clazz].
     * @param clazz the class to register a listener for
     * @param consumer the consumer that will be triggered by the server when it is ready to be consumed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : IncomingMessage> addListener(
        clazz: Class<out T>,
        consumer: BiConsumer<R, in T>,
    ) {
        val old = consumers.put(clazz, consumer as BiConsumer<R, in IncomingMessage>)
        require(old == null) {
            "Overwriting old listener for class $clazz"
        }
    }

    /**
     * Adds a listener for the provided [T]; this function is an overload of the [addListener],
     * intended to make registering listeners from Kotlin easier
     * @param listener the listener of the message.
     */
    @JvmSynthetic
    public inline fun <reified T : IncomingMessage> addListener(crossinline listener: R.(message: T) -> Unit) {
        addListener(T::class.java) { r, t -> listener(r, t) }
    }

    public fun build(): GameMessageConsumerRepository<R> {
        return GameMessageConsumerRepository(consumers.toMap(HashMap()))
    }
}
