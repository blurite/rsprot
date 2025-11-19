package net.rsprot.protocol.binary

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.pVarInt
import net.rsprot.buffer.extensions.pdata
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.protocol.Prot
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

public class BinaryStream(
    private var buffer: ByteBuf,
    private var nanoTime: Long = 0,
) {
    private val lockCount: AtomicInteger = AtomicInteger(0)

    /**
     * Appends a packet into the buffer in this stream.
     * @param serverToClient whether the packet comes from server or from client
     * @param opcode the unobfuscated, non-encrypted opcode
     * @param size the constant size of the packet. For var-byte it's always -1, for var-short it's always -2.
     * @param payload the payload buffer of the packet.
     */
    @Synchronized
    public fun append(
        serverToClient: Boolean,
        opcode: Int,
        size: Int,
        payload: ByteBuf,
    ) {
        try {
            putData(
                serverToClient,
                opcode,
                size,
                payload,
            )
        } finally {
            payload.release()
        }
    }

    /**
     * Appends a packet into the buffer in this stream.
     * @param serverToClient whether the packet comes from server or from client
     * @param opcode the unobfuscated, non-encrypted opcode
     * @param size the constant size of the packet. For var-byte it's always -1, for var-short it's always -2.
     * @param payload the payload buffer of the packet.
     * @return a callback (realSize) allowing one to overwrite the previously written payload.
     * This is specifically used for packet groups which can't have their payload known until after
     * the rest of the packets have been written.
     */
    @Synchronized
    public fun appendWithSizeCallback(
        serverToClient: Boolean,
        opcode: Int,
        size: Int,
        payload: ByteBuf,
    ): Consumer<Int> {
        try {
            val marker =
                putData(
                    serverToClient,
                    opcode,
                    size,
                    payload,
                )
            lockCount.incrementAndGet()
            return Consumer { realSize ->
                lockCount.decrementAndGet()
                overwritePayload(marker, realSize)
            }
        } finally {
            payload.release()
        }
    }

    /**
     * Appends a packet into the buffer in this stream.
     * @param serverToClient whether the packet comes from server or from client
     * @param opcode the unobfuscated, non-encrypted opcode
     * @param size the constant size of the packet. For var-byte it's always -1, for var-short it's always -2.
     * @param payload the payload buffer of the packet.
     */
    private fun putData(
        serverToClient: Boolean,
        opcode: Int,
        size: Int,
        payload: ByteBuf,
    ): Int {
        val directionOpcode = if (serverToClient) 1 else 0
        val previousPacketNanoTime = this.nanoTime
        val currentPacketNanoTime = System.nanoTime()
        this.nanoTime = currentPacketNanoTime
        val nanoDelta = max(0, currentPacketNanoTime - previousPacketNanoTime)
        val millisecondDelta = nanoDelta / NANOSECONDS_IN_MILLISECOND
        val delta = min(MAX_31BIT_INT, millisecondDelta).toInt()
        val bitpacked = directionOpcode or (delta shl 1)
        this.buffer.pVarInt(bitpacked)
        if (opcode < 0x80) {
            this.buffer.p1(opcode)
        } else {
            this.buffer.p1(opcode ushr 8 or 0x80)
            this.buffer.p1(opcode and 0xFF)
        }
        if (size == Prot.VAR_BYTE) {
            this.buffer.p1(payload.readableBytes())
        } else if (size == Prot.VAR_SHORT) {
            this.buffer.p2(payload.readableBytes())
        }
        val marker = this.buffer.writerIndex()
        this.buffer.pdata(payload)
        return marker
    }

    /**
     * Overwrites the payload of a packet group.
     * @param marker the writer index where the packet payload was previously written at.
     * @param realSize the real size of the packet group.
     */
    @Synchronized
    private fun overwritePayload(
        marker: Int,
        realSize: Int,
    ) {
        val curIndex = this.buffer.writerIndex()
        this.buffer.writerIndex(marker)
        this.buffer.p2(realSize)
        this.buffer.writerIndex(curIndex)
    }

    /**
     * Captures a full snapshot of the stream buffer without modifying the buffer.
     * @return a retained slice of the full buffer of the stream, or null if the buffer has a lock on it,
     * implying some previous part of it will be modified at a future date.
     */
    @Synchronized
    public fun fullSnapshotOrNull(): ByteArray? {
        if (lockCount.get() > 0) return null
        val buffer = this.buffer
        val array = ByteArray(buffer.readableBytes())
        buffer.getBytes(buffer.readerIndex(), array)
        return array
    }

    /**
     * Captures a partial snapshot of the stream buffer, resetting the backing buffer afterward.
     * @return a retained slice of the full buffer of the stream, or null if the buffer has a lock on it,
     * implying some previous part of it will be modified at a future date.
     */
    @Synchronized
    public fun incrementalSnapshotOrNull(): ByteArray? {
        if (lockCount.get() > 0) return null
        val buffer = this.buffer
        val array = buffer.toByteArray()
        buffer.readerIndex(0)
        buffer.writerIndex(0)
        return array
    }

    /**
     * @return the number of readable bytes in this buffer.
     */
    @Synchronized
    public fun readableBytes(): Int {
        return this.buffer.readableBytes()
    }

    private companion object {
        private const val MAX_31BIT_INT: Long = 1 shl 30
        private const val NANOSECONDS_IN_MILLISECOND: Long = 1_000_000
    }
}
