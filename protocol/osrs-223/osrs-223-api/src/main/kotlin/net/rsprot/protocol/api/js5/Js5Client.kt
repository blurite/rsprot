package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.api.js5.util.IntArrayDeque
import net.rsprot.protocol.api.logging.js5Log
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.incoming.UrgentRequest
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import kotlin.math.min

/**
 * The JS5 client is responsible for keeping track of all the requests and state of
 * a connected client.
 * @property ctx the channel handler context behind this client
 * @property urgent the array deque for any urgent requests - if any exist, these will be served
 * before the prefetch requests
 * @property prefetch the array deque for any prefetch requests
 * @property currentRequest the current partial request, since the service allows fair serving,
 * we may only write a sector of a group at a time, rather than the full thing
 * @property priority the current priority of this client. If the client is logged in,
 * they will have a higher priority and the service will by default write 3x as much data
 * to that channel compared to any other client that is set to a low, logged-out priority.
 * @property writtenByteCount the number of bytes written since the last flush
 * @property writtenGroupCount the number of group writes that have completed since
 * the last flush
 */
public class Js5Client(
    public val ctx: ChannelHandlerContext,
) {
    private val urgent: IntArrayDeque = IntArrayDeque(MAX_QUEUE_SIZE)
    private val prefetch: IntArrayDeque = IntArrayDeque(MAX_QUEUE_SIZE)
    private val awaitingPrefetch: IntArrayDeque = IntArrayDeque(MAX_QUEUE_SIZE)
    private val currentRequest: PartialJs5GroupRequest = PartialJs5GroupRequest()
    private var lowPriorityChangeCount: Int = 0
    public var priority: ClientPriority = ClientPriority.LOW
        private set

    private var writtenByteCount: Int = 0
    private var writtenGroupCount: Int = 0
    private var xorKey: Int = 0

    /**
     * Gets the next block response for this channel, typically a section of a cache group.
     * @param provider the provider for JS5 groups
     * @param blockLength the maximum size of a block to write in a single call.
     * If the number of bytes left in this group to write is less than the block,
     * then less bytes are written than expected.
     * @return a group response to write to the client, or null if none exists
     */
    public fun getNextBlock(
        provider: Js5GroupProvider,
        blockLength: Int,
    ): Js5GroupResponse? {
        var block: ByteBuf? = currentRequest.block
        if (block == null || currentRequest.isComplete()) {
            val request = pop()
            if (request == -1) {
                return null
            }
            val archiveId = request ushr 16
            val groupId = request and 0xFFFF
            js5Log(logger) {
                "Assigned next request block: $archiveId:$groupId"
            }
            block = provider.provide(archiveId, groupId)
            currentRequest.set(block)
        }
        val progress = currentRequest.progress
        val length = currentRequest.getNextBlockLengthAndIncrementProgress(blockLength)
        writtenByteCount += length
        if (currentRequest.isComplete()) {
            writtenGroupCount++
        }
        return Js5GroupResponse(
            block,
            progress,
            length,
            xorKey,
        )
    }

    /**
     * Pushes a JS5 request to this client, adding it to the end of the respective queue.
     * If this is an urgent request, the request itself is removed from the prefetch list,
     * if it exists. This is a one-way operation, however, as the client by default
     * can only request duplicate requests via this manner. Any modifications to the client
     * can by-pass this, but that is a non issue since we offer a fair JS5 service where
     * the actual request doesn't matter and the number of bytes written is all the same
     * to everyone connected.
     * @param request the request to add to this client
     */
    public fun push(request: Js5GroupRequest) {
        val bitpacked = request.bitpacked
        if (request is UrgentRequest) {
            prefetch.remove(bitpacked)
            awaitingPrefetch.remove(bitpacked)
            urgent.addLast(bitpacked)
        } else {
            // If on login screen (NOT pre-login screen), do not throttle prefetch
            if (lowPriorityChangeCount >= 2 && priority == ClientPriority.LOW) {
                this.prefetch.addLast(bitpacked)
            } else {
                awaitingPrefetch.addLast(bitpacked)
            }
        }
    }

    /**
     * Pops a request from this client, prioritizing urgent requests before prefetch.
     * @return the bitpacked id of the request, or -1 if the queues are empty.
     */
    private fun pop(): Int {
        val urgent = urgent.removeFirstOrDefault(-1)
        if (urgent != -1) {
            return urgent
        }
        return prefetch.removeFirstOrDefault(-1)
    }

    /**
     * Transfers [threshold] worth of bytes of prefetch requests to be served
     * to the client.
     * @param groupProvider the provider for JS5 group sizes, allowing for proper throttling
     * @param threshold the threshold at which the loop breaks, stopping any more bytes
     * being transmitted via prefetch than described here
     * @return whether any bytes were transferred to be served to the client.
     */
    internal fun transferPrefetch(
        groupProvider: Js5GroupProvider,
        threshold: Int,
    ): Boolean {
        // Only transfer prefetch over if the connection is effectively idle
        if (awaitingPrefetch.isEmpty() ||
            urgent.isNotEmpty() ||
            prefetch.isNotEmpty() ||
            !currentRequest.isComplete() ||
            !ctx.channel().isActive ||
            !ctx.channel().isWritable
        ) {
            return false
        }
        var transferredBytes = 0
        while (true) {
            val next = awaitingPrefetch.removeFirstOrDefault(-1)
            if (next == -1) {
                break
            }
            val archiveId = next ushr 16
            val groupId = next and 0xFFFF
            val size = groupProvider.provide(archiveId, groupId).readableBytes()
            prefetch.addLast(next)
            transferredBytes += size
            // Do not clog the pipeline with more than threshold of prefetch data at a time, as this results
            // in urgent requests being delayed
            if (size >= threshold) {
                break
            }
        }
        return transferredBytes > 0
    }

    /**
     * Transfers all prefetch requests from the throttled collection over to the
     * non-throttled one. This is one whenever the client goes on login-screen.
     */
    private fun transferAllPrefetch() {
        while (true) {
            val next = awaitingPrefetch.removeFirstOrDefault(-1)
            if (next == -1) {
                break
            }
            prefetch.addLast(next)
        }
    }

    /**
     * Sets this client in a low priority mode, meaning it gets served less data
     * than those that have a higher priority.
     * This happens when a player logs out of the game.
     */
    public fun setLowPriority() {
        this.priority = ClientPriority.LOW
        // A bit of a state machine, as client sets priority to low when it first connects,
        // and once more when it reaches the login screen.
        // Since our goal is to give urgent requests max priority before login screen to speed
        // up load times, we use a boolean flag to indicate when to stop throttling prefetch requests.
        // Once the second (or any furthermore) low priority response is received, the throttle is removed.
        if (++lowPriorityChangeCount >= 2) {
            transferAllPrefetch()
        }
    }

    /**
     * Sets this client in a high priority state, meaning it gets served more data than
     * those that have a lower priority.
     * The client switches to high priority when the player logs into the game.
     */
    public fun setHighPriority() {
        this.priority = ClientPriority.HIGH
    }

    /**
     * Sets the pending encryption key for this client.
     * Client sends this when it receives corrupt data for a group, in which case
     * it will close the old socket first, allowing for a new one to be opened.
     * In that new one, the encryption key is first sent out, followed by any requests
     * it was previously waiting on, as those get transferred from the "awaiting response"
     * over to the "awaiting to be requested" map.
     *
     * A potential theory for why this exist is network filters for HTTP traffic.
     * The client can listen to port 443 which is commonly used for HTTP traffic,
     * and some groups in the cache are not compressed at all. If said groups
     * contain normal text that would fail any network filters, such as those
     * set by schools, this could be a way to bypass these filters by re-requesting
     * the data with it re-encrypted, meaning it won't get caught in the filters again.
     * Besides compression, it is possible by pure chance that a sequence of bytes
     * doesn't pass the filters, too.
     * @param key the encryption key to use
     */
    public fun setXorKey(key: Int) {
        xorKey = key
    }

    /**
     * Checks that the JS5 client isn't full and can accept more requests in both queues.
     */
    public fun isNotFull(): Boolean =
        urgent.size < MAX_QUEUE_SIZE && (prefetch.size + awaitingPrefetch.size) < (MAX_QUEUE_SIZE + 1)

    /**
     * Checks if the client is empty of any requests and has no pending request to still write.
     */
    private fun isEmpty(): Boolean = currentRequest.isComplete() && urgent.isEmpty() && prefetch.isEmpty()

    /**
     * Checks that the client is not empty, meaning it has some requests, or a group is half-written.
     */
    public fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * Checks if the client is ready by ensuring it can be written to, and there is some data
     * to be written to the client.
     */
    public fun isReady(): Boolean = ctx.channel().isWritable && isNotEmpty()

    /**
     * Checks if the client needs flushing based on the input thresholds.
     * @param flushThresholdInBytes the number of bytes that must be written since the last flush,
     * before a flush will occur. Note that the flush only occurs in this case if at least one
     * full group has been finished, as there's no reason to flush an incomplete group,
     * the client will not be able to continue anyhow.
     * @param flushThresholdInGroups the number of full groups written to the client before
     * a flush should occur.
     */
    public fun needsFlushing(
        flushThresholdInBytes: Int,
        flushThresholdInGroups: Int,
    ): Boolean =
        writtenGroupCount >= flushThresholdInGroups ||
            (writtenGroupCount > 0 && writtenByteCount >= flushThresholdInBytes) ||
            (writtenByteCount > 0 && isEmpty()) ||
            !ctx.channel().isWritable

    /**
     * Resets the number of bytes and groups written.
     */
    public fun resetTracker() {
        writtenByteCount = 0
        writtenGroupCount = 0
    }

    /**
     * A class for tracking partial JS5 group requests, allowing us to feed the groups
     * sections at a time, instead of the full thing - this is due to the groups
     * varying greatly in size and naively sending a group could open it up for attacks.
     * @property block the current block that is being written
     * @property progress the current number of bytes written of this block
     * @property length the total number of bytes of this block until it has been
     * completely written over.
     */
    public class PartialJs5GroupRequest {
        public var block: ByteBuf? = null
            private set
        public var progress: Int = 0
            private set
        private var length: Int = 0

        /**
         * Checks whether this group has been fully written over to the client
         */
        public fun isComplete(): Boolean = progress >= length

        /**
         * Gets the length of the next block, capped to [blockLength],
         * and increments the current progress by that value.
         */
        public fun getNextBlockLengthAndIncrementProgress(blockLength: Int): Int {
            val progress = this.progress
            this.progress = min(this.length, this.progress + blockLength)
            return this.progress - progress
        }

        /**
         * Sets a new block to be written to the client, resetting the progress and
         * updating the length to that of this block.
         */
        public fun set(block: ByteBuf) {
            this.block = block
            this.progress = 0
            this.length = block.readableBytes()
        }
    }

    /**
     * The possible client priority values.
     * @property LOW is used when the client is logged out, e.g. in the loading
     * screen or at the login screen
     * @property HIGH is used when the client is logged into the game.
     */
    public enum class ClientPriority {
        LOW,
        HIGH,
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()

        /**
         * The maximum number of requests the client can send out per each group at a time.
         */
        private const val MAX_QUEUE_SIZE: Int = 200
    }
}
