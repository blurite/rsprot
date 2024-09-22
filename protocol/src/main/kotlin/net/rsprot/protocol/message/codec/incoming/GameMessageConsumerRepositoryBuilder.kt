package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Message consumer repository is a repository of listeners that the server will register
 * for various incoming messages. As there can be multiple clients, these cannot directly
 * be stored with the respective decoders.
 *
 * @property consumers the hashmap of consumers, using incoming message classes as keys.
 */
public class GameMessageConsumerRepositoryBuilder<R> {
    private val consumers: MutableMap<Class<out IncomingGameMessage>, MessageConsumer<R, IncomingGameMessage>> =
        HashMap()
    private val globalConsumers: MutableList<MessageConsumer<R, IncomingGameMessage>> = ArrayList()

    /**
     * Adds a listener for the provided [clazz].
     * @param clazz the class to register a listener for
     * @param consumer the consumer that will be triggered by the server when it is ready to be consumed.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : IncomingGameMessage> addListener(
        clazz: Class<out T>,
        consumer: MessageConsumer<R, T>,
    ): GameMessageConsumerRepositoryBuilder<R> {
        val old =
            consumers.put(
                clazz,
                consumer as MessageConsumer<R, IncomingGameMessage>,
            )
        require(old == null) {
            "Overwriting old listener for class $clazz"
        }
        return this
    }

    /**
     * Adds a listener for any type of [IncomingGameMessage].
     * These listeners are always invoked _after_ listeners registered for specific message types with [addListener].
     */
    public fun addGlobalListener(
        consumer: MessageConsumer<R, IncomingGameMessage>,
    ): GameMessageConsumerRepositoryBuilder<R> {
        globalConsumers.add(consumer)
        return this
    }

    /**
     * Adds a listener for the provided [T]; this function is an overload of the [addListener],
     * intended to make registering listeners from Kotlin easier
     * @param listener the listener of the message.
     */
    @JvmSynthetic
    public inline fun <reified T : IncomingGameMessage> addListener(
        crossinline listener: R.(message: T) -> Unit,
    ): GameMessageConsumerRepositoryBuilder<R> = addListener(T::class.java) { r, t -> listener(r, t) }

    public fun build(): GameMessageConsumerRepository<R> =
        GameMessageConsumerRepository(
            consumers.toMap(HashMap()),
            globalConsumers.toList(),
        )
}
