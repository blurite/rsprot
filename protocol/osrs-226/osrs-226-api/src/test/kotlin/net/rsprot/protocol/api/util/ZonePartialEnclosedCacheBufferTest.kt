package net.rsprot.protocol.api.util

import io.netty.buffer.Unpooled
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.util.OpFlags
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChange
import net.rsprot.protocol.game.outgoing.zone.payload.LocAnim
import net.rsprot.protocol.game.outgoing.zone.payload.LocDel
import net.rsprot.protocol.game.outgoing.zone.payload.LocMerge
import net.rsprot.protocol.game.outgoing.zone.payload.MapAnim
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim
import net.rsprot.protocol.game.outgoing.zone.payload.ObjAdd
import net.rsprot.protocol.game.outgoing.zone.payload.ObjCount
import net.rsprot.protocol.game.outgoing.zone.payload.ObjDel
import net.rsprot.protocol.game.outgoing.zone.payload.ObjEnabledOps
import net.rsprot.protocol.game.outgoing.zone.payload.SoundArea
import net.rsprot.protocol.message.ZoneProt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class ZonePartialEnclosedCacheBufferTest {
    @Test
    fun `computeZone creates buffers for supported clients`() {
        val cache = ZonePartialEnclosedCacheBuffer()
        val encoders = ZonePartialEnclosedCacheBuffer.createEncoderMap()
        val buffers = cache.computeZone(emptyList())

        assertEquals(encoders.toClientList().toSet(), buffers.keys.toSet())

        // `computeZone` did not receive any zone prot to encode, so all buffers should be empty.
        val expectedBuffers = buffers.map { Unpooled.wrappedBuffer(ByteArray(0)) }
        assertEquals(expectedBuffers, buffers.values.toList())
    }

    @Test
    fun `compute zone partial enclosed buffers`() {
        val cache = ZonePartialEnclosedCacheBuffer()

        val encoders = ZonePartialEnclosedCacheBuffer.createEncoderMap()
        val zoneProt = createFullZoneProtList()
        val buffers = cache.computeZone(zoneProt)

        // Each zone prot should _at minimum_ write their `indexedEncoder` id. (written as a byte)
        // Ensuring every zone prot opcode/payload is written correctly falls out of scope for this test.
        val expectedMinReadableBytes = zoneProt.size * Byte.SIZE_BYTES
        for ((client, buffer) in buffers) {
            assertTrue(buffer.readableBytes() >= expectedMinReadableBytes) {
                "Expected `$expectedMinReadableBytes` readable bytes from " +
                    "buffer for client: $client. (bytes=${buffer.readableBytes()})"
            }
        }

        // Each supported client type should have added a buffer to `activeCachedBuffers`.
        assertEquals(encoders.toClientList().size, buffers.size)

        // The leak-reference-counter should have been incremented by a single zone.
        assertEquals(1, cache.currentZoneComputationCount)
    }

    @Test
    fun `releaseBuffers resets computation count and releases buffers correctly`() {
        val cache = ZonePartialEnclosedCacheBuffer(listOf(OldSchoolClientType.DESKTOP))

        val emptyBuffer = Unpooled.wrappedBuffer(ByteArray(0))
        cache.activeCachedBuffers += emptyBuffer
        cache.currentZoneComputationCount = 1

        cache.releaseBuffers()

        assertEquals(0, cache.activeCachedBuffers.size)
        assertEquals(0, cache.currentZoneComputationCount)
        assertEquals(0, cache.retainedBufferReferences.size)
    }

    @Test
    fun `retain buffers that cannot be released`() {
        val cache = ZonePartialEnclosedCacheBuffer(listOf(OldSchoolClientType.DESKTOP))

        val threshold = ZonePartialEnclosedCacheBuffer.DEFAULT_BUF_RETENTION_COUNT_BEFORE_RELEASE
        val retainedBuffers = (0..<threshold).map { Unpooled.buffer().retain() }

        try {
            for (buffer in retainedBuffers) {
                cache.activeCachedBuffers += buffer
                cache.currentZoneComputationCount++
                cache.releaseBuffers()
            }

            assertEquals(threshold, cache.retainedBufferReferences.size)
            assertEquals(0, cache.activeCachedBuffers.size)
            assertEquals(0, cache.currentZoneComputationCount)
        } finally {
            retainedBuffers.forEach { it.release(2) }
        }
        check(retainedBuffers.all { it.refCnt() == 0 })
    }

    @Test
    fun `force release retained buffers when threshold is reached`() {
        val cache = ZonePartialEnclosedCacheBuffer(listOf(OldSchoolClientType.DESKTOP))

        val threshold = ZonePartialEnclosedCacheBuffer.DEFAULT_BUF_RETENTION_COUNT_BEFORE_RELEASE
        val retainedBuffersSplit1 = (0..<threshold).map { Unpooled.buffer().retain() }
        val retainedBuffersSplit2 = (0..<threshold).map { Unpooled.buffer().retain() }
        val retainedBuffers = retainedBuffersSplit1 + retainedBuffersSplit2

        try {
            // Fill up the retained buffer reference collection.
            for (buffer in retainedBuffersSplit1) {
                cache.activeCachedBuffers += buffer
                cache.currentZoneComputationCount++
                cache.releaseBuffers()
            }
            assertEquals(threshold, cache.retainedBufferReferences.size)
            assertEquals(retainedBuffersSplit1, cache.retainedBufferReferences.flatten())

            // The second slice of buffers should overtake the initial retained buffers.
            for (buffer in retainedBuffersSplit2) {
                cache.activeCachedBuffers += buffer
                cache.currentZoneComputationCount++
                cache.releaseBuffers()
            }
            assertEquals(threshold, cache.retainedBufferReferences.size)
            assertEquals(retainedBuffersSplit2, cache.retainedBufferReferences.flatten())

            // The first slice of buffers should have been forced released via `releaseBuffers`
            // with the `forceRelease` flag.
            assertTrue(retainedBuffersSplit1.none { it.refCnt() != 0 })
        } finally {
            retainedBuffers.forEach { buffer ->
                if (buffer.refCnt() > 0) {
                    buffer.release(buffer.refCnt())
                }
            }
        }
        check(retainedBuffers.all { it.refCnt() == 0 })
    }

    private fun <T> ClientTypeMap<T>.toClientList(): List<OldSchoolClientType> =
        OldSchoolClientType.entries.filter { it in this }

    private fun createFullZoneProtList(): List<ZoneProt> =
        listOf(
            LocAddChange(id = 123, xInZone = 0, zInZone = 0, shape = 0, rotation = 0, OpFlags.ALL_SHOWN),
            LocAnim(id = 123, xInZone = 0, zInZone = 0, shape = 0, rotation = 0),
            LocDel(xInZone = 0, zInZone = 0, shape = 0, rotation = 0),
            LocMerge(
                index = 0,
                id = 123,
                xInZone = 0,
                zInZone = 0,
                shape = 0,
                rotation = 0,
                start = 0,
                end = 0,
                minX = 0,
                minZ = 0,
                maxX = 0,
                maxZ = 0,
            ),
            MapAnim(id = 123, delay = 0, height = 0, xInZone = 0, zInZone = 0),
            MapProjAnim(
                id = 123,
                startHeight = 0,
                endHeight = 0,
                startTime = 0,
                endTime = 0,
                angle = 0,
                progress = 0,
                sourceIndex = 1,
                targetIndex = 1,
                xInZone = 0,
                zInZone = 0,
                deltaX = 0,
                deltaZ = 0,
            ),
            ObjAdd(id = 123, quantity = 0, xInZone = 0, zInZone = 0, opFlags = OpFlags.ALL_SHOWN),
            ObjCount(id = 123, oldQuantity = 0, newQuantity = 0, xInZone = 0, zInZone = 0),
            ObjDel(id = 123, quantity = 0, xInZone = 0, zInZone = 0),
            ObjEnabledOps(id = 123, opFlags = OpFlags.ALL_SHOWN, xInZone = 0, zInZone = 0),
            SoundArea(id = 123, delay = 0, loops = 0, radius = 0, size = 0, xInZone = 0, zInZone = 0),
        )
}
