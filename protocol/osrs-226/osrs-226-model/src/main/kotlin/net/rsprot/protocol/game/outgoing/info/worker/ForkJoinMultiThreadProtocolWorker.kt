package net.rsprot.protocol.game.outgoing.info.worker

import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

/**
 * A simple single-threaded info worker, executing all the callables using [ForkJoinPool].
 * The pool will be used even if there are very few callables to execute.
 */
public class ForkJoinMultiThreadProtocolWorker : ProtocolWorker {
    override fun execute(callables: List<Callable<Unit>>) {
        ForkJoinPool.commonPool().invokeAll(callables)
    }
}
