package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
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
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.measureTime

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(3)
class PlayerInfoBenchmark {
    private lateinit var protocol: PlayerInfoProtocol
    private lateinit var players: Array<PlayerInfo?>
    private val random: ThreadLocalRandom get() = ThreadLocalRandom.current()

    @Setup
    fun setup() {
        protocol = PlayerInfoProtocol(2048, PooledByteBufAllocator.DEFAULT)
        players = arrayOfNulls(2048)
        for (i in 1..<2047) {
            val player = protocol.alloc(i)
            players[i] = player
            player.updateCoord(0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
        }
        postUpdate()
    }

    private fun postUpdate() {
        protocol.postUpdate()
    }

    private fun tick() {
        for (i in 1..<2047) {
            val player = checkNotNull(players[i])
            player.updateCoord(0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
        }
        protocol.prepare()
        protocol.putBitcodes()
        for (i in 1..<2047) {
            val player = checkNotNull(players[i])
            player.backingBuffer().release()
        }
        postUpdate()
    }

    private fun random(excl: Int): Int {
        return Random.Default.nextInt(excl)
    }

    @Benchmark
    fun benchmark() {
        tick()
    }
}

fun main() {
    val bench = PlayerInfoBenchmark()
    bench.setup()
    val count = 50
    val time =
        measureTime {
            repeat(count) {
                bench.benchmark()
            }
        }
    println(time / count)
}
