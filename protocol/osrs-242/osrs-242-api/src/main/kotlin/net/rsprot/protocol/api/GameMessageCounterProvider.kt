package net.rsprot.protocol.api

/**
 * Gets the message counter provider for incoming game messages.
 * This is in a provider implementation as one instance is allocated
 * for each session object.
 */
public fun interface GameMessageCounterProvider {
    /**
     * Provides a game message counter implementation.
     * A new instance must be allocated with each request,
     * as this is per session basis.
     */
    public fun provide(): GameMessageCounter
}
