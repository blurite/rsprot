package net.rsprot.protocol.internal

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.threads.IllegalThreadAccessException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * The thread from which the server is allowed to communicate with RSProt.
 */
private val communicationThread: AtomicReference<Thread> = AtomicReference()

/**
 * Whether to warn on a thread violation error. Default value is true.
 */
private val warnOnError: AtomicBoolean = AtomicBoolean(true)

private val logger: InlineLogger = InlineLogger()

/**
 * Checks whether the calling thread is allowed to continue from this point
 * onward.
 * If the [communicationThread] is assigned, and it does not match with the
 * caller thread, an [IllegalThreadAccessException] will be thrown.
 * @throws IllegalThreadAccessException
 */
public fun checkCommunicationThread() {
    val thread = communicationThread.get() ?: return
    if (Thread.currentThread() === thread) return
    val exception =
        IllegalThreadAccessException(
            "Invalid access from thread ${Thread.currentThread()}, only $thread allowed.",
        )
    if (warnOnError.get()) {
        logger.warn(exception) {
            "Thread violation error"
        }
    } else {
        throw exception
    }
}

/**
 * Sets the thread which is permitted to communicate with RSProt's thread-unsafe
 * properties. If set to null, all threads are allowed to communicate again.
 * Note that while atomic instances are used for [communicationThread] and [warnOnError],
 * they are not atomic as they get updated individually. Our goal is just to ensure that
 * all threads read the latest value and don't use CPU cache (effectively just @Volatile)
 * @param thread the thread permitted to communicate with RSProt's thread-unsafe functions.
 * @param warn whether to warn on a thread violation error, rather than throwing an exception.
 */
public fun setCommunicationThread(
    thread: Thread?,
    warn: Boolean,
) {
    communicationThread.set(thread)
    warnOnError.set(warn)
}
