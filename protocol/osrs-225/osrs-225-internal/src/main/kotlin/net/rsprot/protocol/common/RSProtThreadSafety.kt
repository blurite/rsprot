package net.rsprot.protocol.common

import net.rsprot.protocol.threads.IllegalThreadAccessException
import java.util.concurrent.atomic.AtomicReference

/**
 * The thread from which the server is allowed to communicate with RSProt.
 */
private val communicationThread: AtomicReference<Thread> = AtomicReference()

/**
 * Checks whether the calling thread is allowed to continue from this point
 * onward.
 * If the [communicationThread] is assigned, and it does not match with the
 * caller thread, an [IllegalThreadAccessException] will be thrown.
 * @throws IllegalThreadAccessException
 */
public fun checkCommunicationThread() {
    val thread = communicationThread.get() ?: return
    if (Thread.currentThread() !== thread) {
        throw IllegalThreadAccessException(
            "Invalid access from thread ${Thread.currentThread()}, only $thread allowed.",
        )
    }
}

/**
 * Sets the thread which is permitted to communicate with RSProt's thread-unsafe
 * properties. If set to null, all threads are allowed to communicate again.
 * @param thread the thread permitted to communicate with RSProt's thread-unsafe functions.
 */
public fun setCommunicationThread(thread: Thread?) {
    communicationThread.set(thread)
}
