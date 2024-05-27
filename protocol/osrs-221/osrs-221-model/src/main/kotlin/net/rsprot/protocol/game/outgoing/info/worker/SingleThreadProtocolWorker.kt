package net.rsprot.protocol.game.outgoing.info.worker

import java.util.concurrent.Callable

/**
 * A simple single-threaded info worker, executing all the callables in order on the calling thread.
 */
public class SingleThreadProtocolWorker : ProtocolWorker {
    override fun execute(callables: List<Callable<Unit>>) {
        for (callable in callables) {
            callable.call()
        }
    }
}
