package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.ByteBuf
import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted
import net.rsprot.protocol.common.RSProtFlags
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The byte buf recycler is responsible for releasing any pooled byte buffers which were
 * not safely released by the server. This is due to the library relying on the server to
 * write the pre-calculated buffers to Netty, which would then do the releasing. If the buffers
 * are calculated, but never written to Netty, we would otherwise run into issues with byte buffers
 * leaking.
 * @property forceReleaseThreshold the number of cycles a buffer can be alive for before forcibly
 * cleaning by the recycler. The default is 50 cycles, meaning 30 seconds.
 * @property cycleCount the current number of cycles that the recycler has gone through.
 * @property buffers a concurrent queue of tracked buffers.
 */
internal class ByteBufRecycler(
    private val forceReleaseThreshold: Int = RSProtFlags.byteBufRecyclerCycleThreshold,
) {
    private var cycleCount: Int = 0
    private val buffers: Queue<RecycledByteBuf> = ConcurrentLinkedQueue()

    /**
     * Adds a buffer to the recycler, ensuring it will eventually get safely released back
     * into the pool. Buffers which have no references will not be added.
     * @param buffer the byte buffer to be tracked and safely released in due time.
     */
    fun add(buffer: ByteBuf) {
        // If the buffer is already released, or it is not something that gets pooled anyway,
        // avoid storing it in our tracking mechanism.
        // Non-pooled buffers will always report a non-positive ref count.
        if (buffer.refCnt() <= 0) {
            return
        }
        buffers.add(RecycledByteBuf(cycleCount, buffer.retain()))
    }

    operator fun plusAssign(buffer: ByteBuf) {
        add(buffer)
    }

    /**
     * Increments the cycle count and iterates through all tracked buffers, clearing out any
     * which were previously already released, and forcibly releasing any which have expired.
     */
    fun cycle() {
        val cycle = cycleCount++
        val iterator = buffers.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val elapsed = cycle - next.cycle
            val refCount = next.refCnt()
            if (refCount <= 1 || elapsed >= forceReleaseThreshold) {
                ReferenceCountUtil.safeRelease(next, refCount)
                iterator.remove()
            }
        }
    }

    /**
     * An object that wraps pooled byte buffers which need to be released in the future.
     * @property cycle the [net.rsprot.protocol.game.outgoing.info.ByteBufRecycler.cycle] on which
     * this byte buffer was added to tracking.
     * @property buffer the pooled byte buffer which needs to be released in the future.
     */
    private data class RecycledByteBuf(
        val cycle: Int,
        val buffer: ByteBuf,
    ) : ReferenceCounted by buffer
}
