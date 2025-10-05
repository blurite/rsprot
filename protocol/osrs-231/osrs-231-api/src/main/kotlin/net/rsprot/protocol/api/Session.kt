package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.channel.hostAddress
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnimV1
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnimV2
import net.rsprot.protocol.game.outgoing.zone.payload.SoundArea
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginClientType
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.codec.incoming.MessageConsumer
import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * The session objects are used to link the player instances together with the respective
 * session instances.
 * @param R the receiver of the game message consumers, typically a player
 * @property ctx the channel handler context behind this session, public in case the server
 * needs to directly manage it
 * @property incomingMessageQueue the message queue for incoming game messages
 * @param outgoingMessageQueueProvider the provider for outgoing message queues.
 * One queue is allocated for each possible game server prot category.
 * @property counter the incoming message counter used to determine whether to stop
 * decoding packets after too many have flooded in over a single cycle.
 * @property consumers the map of incoming game message classes to the consumers of
 * said messages
 * @property loginBlock the login block that resulted in this session being constructed
 * @property incomingGameMessageConsumerExceptionHandler the exception handler responsible
 * for managing any exceptions during the game message processing
 * @property outgoingMessageQueues the array of outgoing game messages, categorized based
 * on the server prots. This is because some packets are given priority and written
 * to the client first, despite often being computed near the end of the cycle.
 * @property hostAddress the inet address behind this connection
 * @property disconnectionHook the disconnection hook to trigger if the channel happens
 * to disconnect. It should be noted that it is the server's responsibility to set
 * the hook after a successful login.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class Session<R>(
    private val trafficMonitor: NetworkTrafficMonitor<*>,
    public val ctx: ChannelHandlerContext,
    private val incomingMessageQueue: Queue<IncomingGameMessage>,
    outgoingMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage>,
    private val counter: GameMessageCounter,
    private val consumers: Map<Class<out IncomingGameMessage>, MessageConsumer<R, IncomingGameMessage>>,
    private val globalConsumers: List<MessageConsumer<R, IncomingGameMessage>>,
    public val loginBlock: LoginBlock<*>,
    private val incomingGameMessageConsumerExceptionHandler: IncomingGameMessageConsumerExceptionHandler<R>,
) {
    private val outgoingMessageQueues: Array<Queue<OutgoingGameMessage>> =
        Array(GameServerProtCategory.COUNT) {
            outgoingMessageQueueProvider.provide()
        }
    public val hostAddress: String = ctx.hostAddress()
    private var disconnectionHook: AtomicReference<Runnable?> = AtomicReference(null)

    @Volatile
    private var channelStatus: ChannelStatus = ChannelStatus.OPEN

    private var lastFlush: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    private var lowPriorityCategoryPacketsDiscarded: AtomicBoolean = AtomicBoolean()

    private var loginTransitionStatus: AtomicInteger = AtomicInteger(0)

    /**
     * Discards any packets which have a [GameServerProtCategory.LOW_PRIORITY_PROT] category.
     *
     * In Old School RuneScape, this is used on logout. Only packets which are marked as high priority
     * category appear to get transmitted on the game cycle on which the player clicks the logout button.
     * This function should only be invoked when the player is guaranteed to be getting logged out.
     *
     * Note that info packets (player info, npc info, worldentity info) appear to also not send out
     * once the player enters the logging out state, even though they are in high priority categories.
     * The server should take care of it in such cases.
     *
     * The effects of this function take place on Netty's event loop threads, specifically when [flush]
     * is invoked. If the low priority category packets are disabled, rather than passing them into
     * the pipeline, the packets will be safely released and discarded.
     * Furthermore, it is worth noting that [flush] can be invoked on RSProt's own volition,
     * when there's a lot of backpressure and everything could not be flushed out in one go.
     */
    @JvmOverloads
    public fun discardLowPriorityCategoryPackets(discard: Boolean = true) {
        lowPriorityCategoryPacketsDiscarded.set(discard)
    }

    private fun updateLastFlush() {
        lastFlush = TimeSource.Monotonic.markNow()
    }

    /**
     * Checks if the channel has gone idle in a limbo state.
     * It is unclear how or why this happens, but it rarely does occur - Netty's hook
     * does not get triggered, leaving the connection in a limbo-open status.
     */
    private fun checkIdle() {
        val elapsed = lastFlush.elapsedNow()
        if (elapsed < limboIdleDuration) return
        logger.warn {
            "Connection ${ctx.channel()} has gone idle in limbo, " +
                "requesting channel close for '${loginBlock.username}'."
        }
        triggerIdleClosing()
    }

    /**
     * Triggers the channel closing due to idling, if it hasn't yet been called.
     */
    internal fun triggerIdleClosing() {
        if (internalRequestClose()) {
            invokeDisconnectionHook()
        }
    }

    /**
     * Queues a game message to be written to the client based on the message's defined
     * category
     * @param message the outgoing game message to queue in its respective message queue
     */
    public fun queue(message: OutgoingGameMessage) {
        queue(message, message.category)
    }

    /**
     * Queues a game message to be written to the client based on the provided message
     * category, in case one wishes to override the categories defined by the library
     * @param message the outgoing game message
     * @param category the category of the queue to put the message in
     */
    public fun queue(
        message: OutgoingGameMessage,
        category: ServerProtCategory,
    ) {
        if (this.channelStatus != ChannelStatus.OPEN) {
            message.safeRelease()
            return
        }
        message.markConsumed()
        if (RSProtFlags.filterMissingPacketsInClient) {
            if (loginBlock.clientType == LoginClientType.DESKTOP && message is SoundArea) {
                throw IllegalArgumentException(
                    "SoundArea packet may only be sent as part of " +
                        "partial enclosed as of revision 221 on Java clients. Packet: $message",
                )
            }
            if (loginBlock.clientType != LoginClientType.DESKTOP && message is MapProjAnimV1) {
                throw IllegalArgumentException(
                    "MapProjAnim packet may only be sent as part of " +
                        "partial enclosed as of revision 225 on C++ clients. Packet: $message",
                )
            }
            if (message is MapProjAnimV2) {
                throw IllegalArgumentException("MapProjAnimV2 can only be sent as part of UpdateZonePartialEnclosed.")
            }
        }
        val categoryId = category.id
        val queue = outgoingMessageQueues[categoryId]
        queue += message
        checkIdle()
    }

    /**
     * Iterates through all the incoming packets over the last cycle and invokes the
     * consumer with the receiver on them. If an exception is caught, it is propagated
     * forward to the respective exception handler.
     * If too many messages were received from the server during the last cycle,
     * the decoder will have stopped accepting any new packets. If that was the case
     * at the end of this function, the packet decoding will resume.
     * @param receiver the receiver on whom to invoke the message consumers,
     * typically a player instance
     * @return the number of consumers that was invoked, this is handy in case
     * one wishes to manually track the idle status serverside and potentially
     * log the player out earlier if no packets are received over a number of cycles.
     */
    public fun processIncomingPackets(receiver: R): Int {
        if (this.channelStatus != ChannelStatus.OPEN) return 0
        var count = 0
        while (true) {
            val packet = pollIncomingMessage() ?: break
            val consumer = consumers[packet::class.java]
            checkNotNull(consumer) {
                "Consumer for packet $packet does not exist."
            }
            networkLog(logger) {
                "Processing incoming game packet from channel '${ctx.channel()}': $packet"
            }
            try {
                consumer.consume(receiver, packet)
            } catch (cause: Throwable) {
                incomingGameMessageConsumerExceptionHandler.exceptionCaught(this, packet, cause)
            }
            if (globalConsumers.isNotEmpty()) {
                for (globalConsumer in globalConsumers) {
                    try {
                        globalConsumer.consume(receiver, packet)
                    } catch (cause: Throwable) {
                        incomingGameMessageConsumerExceptionHandler.exceptionCaught(this, packet, cause)
                    }
                }
            }
            count++
        }
        onPollComplete()
        return count
    }

    /**
     * Sets a disconnection hook to be triggered if the connection to this channel is lost,
     * allowing one to safely log the player out in such case. If the channel has already disconnected
     * by the time this function is invoked, the [hook] will be executed __immediately__.
     * @param hook the hook runnable to invoke if the connection is lost
     * @throws IllegalStateException if a hook was already registered
     */
    public fun setDisconnectionHook(hook: Runnable) {
        val currentHook = this.disconnectionHook
        val assigned = currentHook.compareAndSet(null, hook)
        if (!assigned) {
            throw IllegalStateException("A disconnection hook has already been registered!")
        }
        // Immediately trigger the disconnection hook if the channel already went inactive before
        // the hook could be triggered
        if (!ctx.channel().isActive) {
            if (internalRequestClose()) {
                invokeDisconnectionHook()
            }
        }
    }

    /**
     * Requests the channel to be closed once there's nothing more to write out, and the
     * channel has been flushed. This will furthermore clear any disconnection hook set,
     * to avoid any lingering memory. It will not invoke the disconnection hook.
     * @return whether the channel was open and will be closed in the future.
     */
    public fun requestClose(): Boolean {
        if (this.channelStatus != ChannelStatus.OPEN) {
            return false
        }
        this.disconnectionHook.set(null)
        this.channelStatus = ChannelStatus.CLOSING
        this.stopReading()
        this.flush()
        return true
    }

    /**
     * Requests the channel to be closed once there's nothing more to write out, and the
     * channel has been flushed.
     * @return whether the channel was open and will be closed in the future.
     */
    private fun internalRequestClose(): Boolean {
        if (this.channelStatus != ChannelStatus.OPEN) {
            return false
        }
        this.channelStatus = ChannelStatus.CLOSING
        this.stopReading()
        this.flush()
        return true
    }

    /**
     * Polls one incoming game message from the queue, or null if none exists.
     */
    private fun pollIncomingMessage(): IncomingGameMessage? = incomingMessageQueue.poll()

    /**
     * Resets the incoming message counter and resumes reading.
     */
    private fun onPollComplete() {
        counter.reset()
        resumeReading()
    }

    /**
     * Marks the login transition as complete, meaning we can now write out any packets that
     * were queued up until now.
     * This process is necessary as of Netty 4.2, specifically [this](https://github.com/netty/netty/pull/14705) PR.
     *
     * Quote:
     * > **This also means that some code now moves from the executor of the target context, to the executor of the
     * calling context. This can create different behaviors from Netty 4.1, if the pipeline has multiple handlers, is
     * modified by the handlers during the call, and the handlers use child-executors.**
     *
     * Due to the underlying changes in Netty, it is no longer safe to queue packets up, switch pipeline
     * and queue more packets up. The packets that were queued after the pipeline switch may end up processing
     * and sending out first, as each pipeline handler has its own dedicated executor now, which is subject
     * to the usual race condition issues.
     */
    internal fun onLoginTransitionComplete() {
        val flag =
            this.loginTransitionStatus.getAndUpdate { old ->
                old or LOGIN_TRANSITION_COMPLETE
            }
        if (flag and FLUSH_REQUESTED != 0) {
            flush()
        }
    }

    /**
     * Flushes any queued messages to the client, if any exist.
     * The flushing process takes place in the netty event loop, thus
     * the calls to this function are non-blocking and fast.
     * If not all packets were written due to writability constraints,
     * this function will further be re-triggered when channel writability
     * turns back to true, meaning this function can be called directly
     * from the netty event loop, thus the check inside it.
     */
    public fun flush() {
        if (this.channelStatus == ChannelStatus.CLOSED) {
            return
        }
        if (!ctx.channel().isActive) {
            triggerIdleClosing()
            return
        }
        if (outgoingMessageQueues.all(Queue<OutgoingGameMessage>::isEmpty)) {
            return
        }
        updateLastFlush()
        // If login transition hasn't finished yet, wait.
        // We do this without the write call for extra performance
        if (this.loginTransitionStatus.get() and LOGIN_TRANSITION_COMPLETE == 0) {
            val latest =
                this.loginTransitionStatus.getAndUpdate { old ->
                    old or FLUSH_REQUESTED
                }
            // Secondary check due to race conditions; it may have changed since the preliminary check
            if (latest and LOGIN_TRANSITION_COMPLETE == 0) {
                return
            }
        }
        val eventLoop = ctx.channel().eventLoop()
        if (eventLoop.inEventLoop()) {
            writeAndFlush()
        } else {
            eventLoop.execute {
                writeAndFlush()
            }
        }
    }

    /**
     * Clears all the remaining incoming and outgoing messages, releasing any buffers that were wrapped
     * in a byte buffer holder.
     * This function should be called on logout and whenever a reconnection happens, in order
     * to get rid of any messages that got written to the session, but couldn't be flushed
     * out in time before the session became inactive.
     */
    public fun clear() {
        for (queue in outgoingMessageQueues) {
            while (true) {
                val next = queue.poll() ?: break
                next.safeRelease()
            }
        }
        val incomingQueue = incomingMessageQueue
        while (true) {
            val next = incomingQueue.poll() ?: break
            next.safeRelease()
        }
    }

    /**
     * Writes any messages that can be written based on writability to the channel.
     * The message queues are processed in ascending order, with the highest
     * priority being the first. If the writability turns false half-way through,
     * no more messages are written out - this will be resumed when channel writability
     * changes to true again.
     * At the end of this function call, the channel is flushed.
     */
    private fun writeAndFlush() {
        val channel = ctx.channel()
        categories@ for (category in GameServerProtCategory.entries) {
            val queue = outgoingMessageQueues[category.id]

            // Safely discard any low priority category packets if they are disabled
            if (category == GameServerProtCategory.LOW_PRIORITY_PROT &&
                lowPriorityCategoryPacketsDiscarded.get()
            ) {
                while (true) {
                    val next = queue.poll() ?: break
                    next.safeRelease()
                }
                continue
            }

            packets@ while (true) {
                if (!channel.isWritable) {
                    break@categories
                }
                val next = queue.poll() ?: break@packets
                networkLog(logger) {
                    "Writing outgoing game packet to channel '${ctx.channel()}': $next"
                }
                channel.write(next, channel.voidPromise())
            }
        }
        if (this.channelStatus == ChannelStatus.CLOSING) {
            this.channelStatus = ChannelStatus.CLOSED
            trafficMonitor
                .gameChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.hostAddress(),
                    GameDisconnectionReason.LOGOUT,
                )
            channel
                .writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE)
            clear()
            networkLog(logger) {
                "Flushed outgoing game packets to channel '${ctx.channel()}', closing channel."
            }
            return
        }
        channel.flush()
        networkLog(logger) {
            val leftoverPackets = outgoingMessageQueues.sumOf(Queue<OutgoingGameMessage>::size)
            if (leftoverPackets > 0) {
                "Flushing outgoing game packets to channel " +
                    "'${ctx.channel()}': $leftoverPackets leftover packets remaining"
            } else {
                "Flushing outgoing game packets to channel ${ctx.channel()}"
            }
        }
    }

    /**
     * Sets auto-read back to true and single decode back to false.
     */
    internal fun resumeReading() {
        if (!ctx.channel().isOpen) {
            return
        }
        try {
            setReadStatus(stopReading = false)
        } catch (e: Exception) {
            logger.debug(e) { "Unable to continue reading from the channel - channel already closed." }
        }
    }

    /**
     * Sets auto-read back to false and single decode back to true.
     */
    internal fun stopReading() {
        if (!ctx.channel().isOpen) {
            return
        }
        try {
            setReadStatus(stopReading = true)
        } catch (e: Exception) {
            logger.debug(e) { "Unable to stop reading from the channel - channel already closed." }
        }
    }

    /**
     * Sets the auto-read and single decode status based on the input,
     * if the channel is still open.
     * If the single decode is set to false, netty will IMMEDIATELY stop
     * decoding any more packets. Just turning auto-read to false does not
     * have the same behavior, as by default, netty will read until the ctx
     * is empty. This ensures that we only decode exactly up to the packet limit
     * and never any more beyond that.
     * If auto-read is set back to true, netty automatically calls ctx.read(), so
     * we do not need to manually call this.
     * @param stopReading whether to stop reading more packets, or resume reading
     */
    private fun setReadStatus(stopReading: Boolean) {
        val channel = ctx.channel()
        // The decoder will be null if the channel has closed
        val decoder =
            channel.pipeline()[GameMessageDecoder::class.java]
                ?: return
        decoder.isSingleDecode = stopReading
        channel.config().isAutoRead = !stopReading
    }

    /**
     * Adds an incoming message to the incoming message queue.
     * Function is public to assist with testing, and should not be invoked
     * by servers outside of that.
     */
    public fun addIncomingMessage(incomingGameMessage: IncomingGameMessage) {
        if (this.channelStatus != ChannelStatus.OPEN) return
        incomingMessageQueue += incomingGameMessage
    }

    /**
     * Increment the message counter for the provided incoming game message,
     * based on the message's category.
     */
    internal fun incrementCounter(incomingGameMessage: IncomingGameMessage) {
        if (this.channelStatus != ChannelStatus.OPEN) return
        counter.increment(incomingGameMessage.category)
    }

    /**
     * Whether any of the incoming message categories are full, meaning
     * no more packets should be decoded.
     */
    internal fun isFull(): Boolean = counter.isFull()

    /**
     * Invokes the disconnection hook if it isn't null, while also nulling out the property,
     * so that it will never get invoked more than once, even if it ends up getting called from
     * different threads simultaneously.
     */
    private fun invokeDisconnectionHook() {
        this.disconnectionHook.getAndSet(null)?.run()
    }

    private enum class ChannelStatus {
        OPEN,
        CLOSING,
        CLOSED,
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
        private val limboIdleDuration: Duration = 30.seconds
        private const val LOGIN_TRANSITION_COMPLETE: Int = 0x1
        private const val FLUSH_REQUESTED: Int = 0x2
    }
}
