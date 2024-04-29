package net.rsprot.protocol.message.codec.incoming.provider

import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository

/**
 * A default game message consumer repository provider, returning the instance
 * without any blocking or waiting.
 * @property repository the repository to provide
 */
public class DefaultGameMessageConsumerRepositoryProvider<R>(
    private val repository: GameMessageConsumerRepository<R>,
) : GameMessageConsumerRepositoryProvider<R> {
    override fun provide(): GameMessageConsumerRepository<R> {
        return repository
    }
}
