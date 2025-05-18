package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Packet group start is a packet which tells the client to wait until the entire
 * payload of a packet group has arrived, then process all of it in a single client cycle,
 * bypassing the usual 100 packets per client cycle limitation that the client has.
 * @property messages the messages to wait for and process instantly. Note that the
 * size of all these messages combined must be <= 32,767 bytes. Exceeding this limit
 * will cause the protocol to crash for that user, disconnecting them. This is due to
 * ISAAC cipher being modified during the encoding of the payload, which we cannot
 * recover from without complex state tracking, which will not be supported.
 */
public class PacketGroupStart(
    public val messages: List<OutgoingGameMessage>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun estimateSize(): Int {
        // Always assume the highest possible size here, the buffers are pooled anyway.
        // We cannot just sum up the estimations from [messages] as some packets are
        // special and handled differently, which would cause unwanted resizing to occur
        return 40_000
    }

    override fun markConsumed() {
        for (message in messages) {
            message.markConsumed()
        }
    }

    override fun safeRelease() {
        for (message in messages) {
            message.safeRelease()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PacketGroupStart

        return messages == other.messages
    }

    override fun hashCode(): Int {
        return messages.hashCode()
    }

    override fun toString(): String {
        return "PacketGroupStart(messages=$messages)"
    }
}
