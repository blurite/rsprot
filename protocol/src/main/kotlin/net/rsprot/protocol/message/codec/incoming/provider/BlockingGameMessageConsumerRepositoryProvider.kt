package net.rsprot.protocol.message.codec.incoming.provider

import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository

/**
 * A blocking game message consumer repository provider.
 * This implementation will block any calling threads until an instance has been provided,
 * after which all waiting threads will be notified.
 * @property lock the lock to wait on
 * @property repository the game message consumer repository instance.
 */
public class BlockingGameMessageConsumerRepositoryProvider<R> : GameMessageConsumerRepositoryProvider<R> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock: Object = Object()

    @Volatile
    private var repository: GameMessageConsumerRepository<R>? = null

    override fun provide(): GameMessageConsumerRepository<R> {
        val repository = this.repository
        if (repository == null) {
            synchronized(lock) {
                lock.wait()
            }
            return checkNotNull(this.repository)
        }
        return repository
    }

    public fun supply(repository: GameMessageConsumerRepository<R>) {
        this.repository = repository
        synchronized(lock) {
            lock.notifyAll()
        }
    }
}
