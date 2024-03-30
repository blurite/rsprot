package net.rsprot.buffer

import io.netty.buffer.Unpooled
import net.rsprot.buffer.bitbuffer.WrappedBitBuf
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(3)
class BitBufBenchmark {
    private lateinit var nettyDirectBitBuf: WrappedBitBuf
    private lateinit var openrs2DirectBitBuffer: OpenRs2BitBuf

    init {
        if (DISABLE_NETTY_CHECKS) {
            System.setProperty("io.netty.buffer.checkBounds", "false")
            System.setProperty("io.netty.buffer.checkAccessible", "false")
        }
        // Error checking should always remain enabled, it is a fairly cheap operation
        System.setProperty("net.rsprot.buffer.bitbufferErrorChecking", "true")
        // Ensuring writable however is very costly, costing us ~50% of the performance
        // It is very likely we can just ignore ensureWritable in production,
        // as all our bit buffers will be 40kb in size, and we can never exceed that anyway.
        System.setProperty("net.rsprot.buffer.bitbufferEnsureWritable", "false")
    }

    @Setup
    fun setup() {
        nettyDirectBitBuf = WrappedBitBuf(Unpooled.directBuffer(BUFFER_SIZE))
        openrs2DirectBitBuffer = OpenRs2BitBuf(Unpooled.directBuffer(BUFFER_SIZE))
    }

    @Benchmark
    fun nettyDirectBitBufferWrite() {
        nettyDirectBitBuf.writerIndex(0)
        nettyDirectBitBuf.use {
            for (i in 0..<ITERATION_COUNT) {
                it.pBits(WRITTEN_BIT_COUNT, 100)
            }
        }
    }

    @Benchmark
    fun openrs2DirectBitBufferWrite() {
        openrs2DirectBitBuffer.writerIndex(0L)
        openrs2DirectBitBuffer.use {
            for (i in 0..<ITERATION_COUNT) {
                it.writeBits(WRITTEN_BIT_COUNT, 100)
            }
        }
    }

    private companion object {
        private const val DISABLE_NETTY_CHECKS = true
        private const val BUFFER_SIZE = 40_000
        private const val WRITTEN_BIT_COUNT = 7
        private const val ITERATION_COUNT = BUFFER_SIZE * Byte.SIZE_BITS / WRITTEN_BIT_COUNT
    }
}
