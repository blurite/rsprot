package net.rsprot.protocol.game.outgoing.info.worker

import java.util.concurrent.Callable

/**
 * Provides an API to processing info protocols.
 */
public interface ProtocolWorker {
    /**
     * Executes the [callables] collection as defined by the given worker's behavior.
     * The callables may be executed asynchronously and are guaranteed to be thread-safe.
     * It should be noted that _all_ the callables must be called upon, or the protocol breaks.
     *
     * @param callables the list of callables that must be executed.
     */
    public fun execute(callables: List<Callable<Unit>>)
}
