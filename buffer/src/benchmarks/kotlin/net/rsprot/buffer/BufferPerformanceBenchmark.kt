package net.rsprot.buffer

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
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
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(3)
class BufferPerformanceBenchmark {
    init {
        if (DISABLE_NETTY_CHECKS) {
            System.setProperty("io.netty.buffer.checkBounds", "false")
            System.setProperty("io.netty.buffer.checkAccessible", "false")
        }
    }

    private lateinit var nettyHeapBuffer: ByteBuf
    private lateinit var nettyDirectBuffer: ByteBuf
    private lateinit var nioHeapBuffer: ByteBuffer
    private lateinit var nioDirectBuffer: ByteBuffer
    private lateinit var byteArray: ByteArray

    @Setup
    fun setup() {
        nettyHeapBuffer = Unpooled.buffer(BUFFER_CAPACITY)
        nettyDirectBuffer = Unpooled.directBuffer(BUFFER_CAPACITY)
        nioHeapBuffer = ByteBuffer.allocate(BUFFER_CAPACITY)
        nioDirectBuffer = ByteBuffer.allocateDirect(BUFFER_CAPACITY)
        byteArray = ByteArray(BUFFER_CAPACITY)
    }

    @Benchmark
    fun nettyHeapBufferRw() {
        nettyHeapBuffer.writerIndex(0)
        for (i in 1..<BUFFER_CAPACITY) {
            nettyHeapBuffer.writeByte(nettyHeapBuffer.getByte(i - 1) + 1)
        }
    }

    @Benchmark
    fun nettyDirectBufferRw() {
        nettyDirectBuffer.writerIndex(0)
        for (i in 1..<BUFFER_CAPACITY) {
            nettyDirectBuffer.writeByte(nettyDirectBuffer.getByte(i - 1) + 1)
        }
    }

    @Benchmark
    fun nioHeapBufferRw() {
        nioHeapBuffer.position(0)
        for (i in 1..<BUFFER_CAPACITY) {
            nioHeapBuffer.put((nioHeapBuffer.get(i - 1).toInt() + 1).toByte())
        }
    }

    @Benchmark
    fun nioDirectBufferRw() {
        nioDirectBuffer.position(0)
        for (i in 1..<BUFFER_CAPACITY) {
            nioDirectBuffer.put((nioDirectBuffer.get(i - 1).toInt() + 1).toByte())
        }
    }

    @Benchmark
    fun byteArrayRw() {
        for (i in 1..<BUFFER_CAPACITY) {
            byteArray[i] = (byteArray[i - 1].toInt() + 1).toByte()
        }
    }

    private companion object {
        private const val BUFFER_CAPACITY = 40_000
        private const val DISABLE_NETTY_CHECKS = true
    }
}
