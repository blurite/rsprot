package net.rsprot.protocol.game.outgoing.info.worker

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

/**
 * The default protocol worker, utilizing the calling thread
 * if there are less than [asynchronousThreshold] callables to execute.
 * Otherwise, utilizes the [executorService] to process the callables in parallel.
 */
public class DefaultProtocolWorker(
    private val asynchronousThreshold: Int,
    private val executorService: ExecutorService,
) : ProtocolWorker {
    /**
     * A default implementation that switches to parallel processing using [ForkJoinPool]
     * if there are at least `coreCount * 4` callables to execute.
     * Otherwise, utilizes the calling thread.
     */
    public constructor() : this(
        Runtime.getRuntime().availableProcessors() * 4,
        ForkJoinPool.commonPool(),
    )

    override fun execute(callables: List<Callable<Unit>>) {
        if (callables.size < asynchronousThreshold) {
            for (callable in callables) {
                callable.call()
            }
        } else {
            executorService.invokeAll(callables)
        }
    }
}
