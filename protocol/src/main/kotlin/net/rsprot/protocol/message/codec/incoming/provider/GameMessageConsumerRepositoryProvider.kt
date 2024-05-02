package net.rsprot.protocol.message.codec.incoming.provider

import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository

/**
 * A provider interface for game message consumer repositories.
 * This is necessary as often times these consumers get loaded up quite late in the
 * server boot process, and it'd be less than ideal to block server boot time
 * behind something as trivial as this, when it isn't even going to be hit until
 * the right moment.
 */
public fun interface GameMessageConsumerRepositoryProvider<R> {
    /**
     * Providers the game message consumer repository.
     * This function may be blocking, depending on the implementation behind it.
     */
    public fun provide(): GameMessageConsumerRepository<R>
}
