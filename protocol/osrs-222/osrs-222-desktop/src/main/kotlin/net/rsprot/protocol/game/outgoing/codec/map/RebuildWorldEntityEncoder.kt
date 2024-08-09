package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.map.RebuildWorldEntity
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class RebuildWorldEntityEncoder : MessageEncoder<RebuildWorldEntity> {
    override val prot: ServerProt = GameServerProt.REBUILD_WORLDENTITY

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildWorldEntity,
    ) {
        buffer.p2(message.index)
        buffer.p2(message.baseX)
        buffer.p2(message.baseZ)
        try {
            buffer.buffer.writeBytes(
                message.gpiInitBlock,
                message.gpiInitBlock.readerIndex(),
                message.gpiInitBlock.readableBytes(),
            )
        } finally {
            message.gpiInitBlock.release()
        }

        // Xtea count, temporary value
        val marker = buffer.writerIndex()
        buffer.p2(0)

        var xteaCount = 0
        val (mapsquares, xteas) = distinctMapsquares.get()
        val maxBitBufByteCount = ((27 * message.zones.size) + 32) ushr 5
        val maxXteaByteCount = 2 + (4 * 4 * message.zones.size)
        // Ensure the correct number of writable bytes ahead of time for the worst case scenario
        // This is due to our bit buffer implementation by default not ensuring this
        buffer.buffer.ensureWritable(maxBitBufByteCount + maxXteaByteCount)
        val bitbuf = buffer.buffer.toBitBuf()
        bitbuf.use {
            for (zone in message.zones) {
                if (zone == null) {
                    bitbuf.pBits(1, 0)
                    continue
                }
                bitbuf.pBits(1, 1)
                bitbuf.pBits(26, zone.referenceZone.packed)
                val mapsquareId = zone.referenceZone.mapsquareId
                if (contains(mapsquares, xteaCount, mapsquareId)) {
                    continue
                }
                mapsquares[xteaCount] = mapsquareId
                xteas[xteaCount] = zone.key
                xteaCount++
            }
        }
        // Write the real xtea count
        val writerIndex = buffer.writerIndex()
        buffer.writerIndex(marker)
        buffer.p2(xteaCount)
        buffer.writerIndex(writerIndex)

        for (i in 0..<xteaCount) {
            val xteaKey = xteas[i]
            for (intKey in xteaKey.key) {
                buffer.p4(intKey)
            }
        }
    }

    /**
     * Check if the [array] contains the [value] in it, up until [length] (exclusive).
     * As our arrays are pre-initialized to a capacity of 676, we do not want to search
     * the entire thing when we have only added a few elements to it.
     * Additionally, since we do not zero out the arrays, anything beyond the [length]
     * would be phantom data from previous packets.
     * @param array the int array to search
     * @param length the length of the array that has been filled up
     * @param value the value to seek for
     * @return whether the int array contains the [value] in the first [length] indices
     */
    private fun contains(
        array: IntArray,
        length: Int,
        value: Int,
    ): Boolean {
        for (i in 0..<length) {
            val element = array[i]
            if (element == value) {
                return true
            }
        }
        return false
    }

    private companion object {
        /**
         * The maximum theoretical number of mapsquares that can be sent in a single
         * rebuild region packet.
         */
        private const val MAX_POTENTIAL_MAPSQUARES = 4 * 13 * 13

        /**
         * A thread-local implementation of mapsquares and their keys.
         * As we need to trim our data set down to distinct mapsquares,
         * doing so with new lists all the time can be quite wasteful, especially
         * knowing how volatile the actual counts can be.
         * To minimize the garbage created (in this case, to none),
         * we use thread-local implementations for distinct mapsquares.
         */
        private val distinctMapsquares =
            ThreadLocal.withInitial {
                IntArray(MAX_POTENTIAL_MAPSQUARES) to
                    Array(4 * 13 * 13) {
                        XteaKey.ZERO
                    }
            }
    }
}
