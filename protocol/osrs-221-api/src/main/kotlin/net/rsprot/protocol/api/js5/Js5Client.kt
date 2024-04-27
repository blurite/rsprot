package net.rsprot.protocol.api.js5

import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.incoming.UrgentRequest
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import kotlin.math.min

public class Js5Client<T : Js5GroupType>(
    public val ctx: ChannelHandlerContext,
) {
    // TODO: Replace the queues with a primitive fixed-capacity variant
    private val urgent = ArrayDeque<Int>()
    private val prefetch = ArrayDeque<Int>()
    private val currentRequest: PartialJs5GroupRequest<T> = PartialJs5GroupRequest()
    private var priority: ClientPriority = ClientPriority.LOW

    @Volatile
    private var writtenByteCount: Int = 0

    public fun getNextBlock(provider: Js5GroupProvider<T>): Js5GroupResponse? {
        var block: T? = currentRequest.block
        if (block == null || currentRequest.isComplete()) {
            val request = pop()
            if (request == -1) {
                return null
            }
            val archiveId = request ushr 16
            val groupId = request and 0xFFFF
            block = provider.provide(archiveId, groupId)
            currentRequest.set(block)
        }
        val progress = currentRequest.progress
        val length = currentRequest.getNextBlockLengthAndIncrementProgress()
        writtenByteCount += length
        return provider.toJs5GroupResponse(block, progress, length)
    }

    public fun push(request: Js5GroupRequest) {
        val bitpacked = request.bitpacked
        if (urgent.contains(bitpacked) || prefetch.contains(bitpacked)) {
            return
        }
        if (request is UrgentRequest) {
            prefetch -= bitpacked
            urgent.addLast(bitpacked)
        } else {
            prefetch.addLast(bitpacked)
        }
    }

    private fun pop(): Int {
        if (!urgent.isEmpty()) {
            return urgent.removeFirst()
        }
        if (!prefetch.isEmpty()) {
            return prefetch.removeFirst()
        }
        return -1
    }

    public fun setLowPriority() {
        this.priority = ClientPriority.LOW
    }

    public fun setHighPriority() {
        this.priority = ClientPriority.HIGH
    }

    public fun setXorKey(key: Int) {
        urgent.clear()
        prefetch.clear()
        // We cannot erase the half-written request just yet
        // This is because the client expects N number of bytes,
        // and we have already written some without the xor key
        // which makes this request invalid regardless, but just
        // to fulfill the byte requirement, we continue on with the old
        // request, knowing it will be invalid.
        ctx
            .channel()
            .attr(ChannelAttributes.XOR_ENCRYPTION_KEY)
            .set(key)
    }

    public fun isNotFull(): Boolean {
        return urgent.size < MAX_QUEUE_SIZE && prefetch.size < MAX_QUEUE_SIZE
    }

    private fun isEmpty(): Boolean {
        return currentRequest.isComplete() && urgent.isEmpty() && prefetch.isEmpty()
    }

    public fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    public fun isReady(): Boolean {
        return ctx.channel().isWritable && isNotEmpty()
    }

    public fun needsFlushing(): Boolean {
        return writtenByteCount > FLUSH_THRESHOLD || (writtenByteCount > 0 && isEmpty())
    }

    public fun resetByteCountTracker() {
        writtenByteCount = 0
    }

    public class PartialJs5GroupRequest<T : Js5GroupType> {
        public var block: T? = null
            private set
        public var progress: Int = 0
            private set
        private var length: Int = 0

        public fun isComplete(): Boolean {
            return progress >= length
        }

        public fun getNextBlockLengthAndIncrementProgress(): Int {
            val progress = this.progress
            this.progress = min(this.length, this.progress + Js5Service.BLOCK_LENGTH)
            return this.progress - progress
        }

        public fun set(block: T) {
            this.block = block
            this.progress = 0
            this.length = block.length
        }
    }

    public enum class ClientPriority {
        LOW,
        HIGH,
    }

    private companion object {
        private const val MAX_QUEUE_SIZE: Int = 200
        private const val FLUSH_THRESHOLD: Int = 10_240
    }
}
