package net.rsprot.buffer

import io.netty.buffer.Unpooled
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
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
class BitBufTransferBenchmark {
    private lateinit var nettyDirectBitBuf: BitBuf
    private lateinit var bufferToCopy: UnsafeLongBackedBitBuf

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
        System.setProperty("net.rsprot.buffer.bitbufferEnsureWritable", "true")
    }

    @Setup
    fun setup() {
        nettyDirectBitBuf = BitBuf(Unpooled.directBuffer(BUFFER_SIZE))
        bufferToCopy = UnsafeLongBackedBitBuf()
        bufferToCopy.pBits(1, 1)
        bufferToCopy.pBits(8, 45)
        bufferToCopy.pBits(8, 80)
        bufferToCopy.pBits(8, 125)
        bufferToCopy.pBits(8, 200)
        bufferToCopy.pBits(5, 10)
    }

    @Benchmark
    fun nettyDirectBitBufferWrite() {
        nettyDirectBitBuf.writerIndex(0)
        nettyDirectBitBuf.use {
            it.pBits(1, 1)
            it.pBits(8, 45)
            it.pBits(8, 80)
            it.pBits(8, 125)
            it.pBits(8, 200)
            it.pBits(5, 10)
        }
    }

    @Benchmark
    fun nettyDirectBitBufferTransfer() {
        nettyDirectBitBuf.writerIndex(0)
        nettyDirectBitBuf.use {
            it.pBits(bufferToCopy)
        }
    }

    private companion object {
        private const val DISABLE_NETTY_CHECKS = true
        private const val BUFFER_SIZE = 40_000
    }
}
