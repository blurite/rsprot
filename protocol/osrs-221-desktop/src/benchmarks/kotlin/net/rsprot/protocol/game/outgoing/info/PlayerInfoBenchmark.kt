package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.AppearanceEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.ChatEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.ExactMoveEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.FaceAngleEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.FacePathingEntityEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.HitEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.MoveSpeedEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.SayEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.SequenceEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.SpotAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.TemporaryMoveSpeedEncoder
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.TintingEncoder
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType
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
import kotlin.random.Random
import kotlin.time.measureTime

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(3)
class PlayerInfoBenchmark {
    private lateinit var protocol: PlayerInfoProtocol
    private lateinit var players: Array<PlayerInfo?>
    private val random: Random = Random(0)

    @Setup
    fun setup() {
        val encoders = mapOf(PlatformType.DESKTOP to getExtendedInfoEncoders())
        protocol =
            PlayerInfoProtocol(
                2048,
                PooledByteBufAllocator.DEFAULT,
                encoders,
                createHuffmanCodec(),
            )
        players = arrayOfNulls(2048)
        for (i in 1..<MAX_IDX) {
            val player = protocol.alloc(i, PlatformType.DESKTOP)
            players[i] = player
            player.updateCoord(0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            initializeAppearance(player, i)
        }
        protocol.prepareExtendedInfo()
        postUpdate()
    }

    private fun initializeAppearance(
        player: PlayerInfo,
        index: Int,
    ) {
        player.extendedInfo.initializeAppearance(
            name = "Bot $index",
            combatLevel = 126,
            skillLevel = 0,
            hidden = false,
            male = true,
            textGender = 0,
            skullIcon = -1,
            overheadIcon = -1,
        )
        for (colIdx in 0..<5) {
            player.extendedInfo.setColour(colIdx, colIdx * 10)
        }
        player.extendedInfo.setIdentKit(8, 0)
        player.extendedInfo.setIdentKit(11, 10)
        player.extendedInfo.setIdentKit(4, 18)
        player.extendedInfo.setIdentKit(6, 26)
        player.extendedInfo.setIdentKit(9, 33)
        player.extendedInfo.setIdentKit(7, 36)
        player.extendedInfo.setIdentKit(10, 42)
        player.extendedInfo.setBaseAnimationSet(
            808,
            823,
            819,
            820,
            821,
            822,
            824,
        )
    }

    private fun getExtendedInfoEncoders(): ExtendedInfoEncoders {
        return ExtendedInfoEncoders(
            AppearanceEncoder(),
            ChatEncoder(),
            ExactMoveEncoder(),
            FaceAngleEncoder(),
            FacePathingEntityEncoder(),
            HitEncoder(),
            MoveSpeedEncoder(),
            SayEncoder(),
            SequenceEncoder(),
            SpotAnimEncoder(),
            TemporaryMoveSpeedEncoder(),
            TintingEncoder(),
        )
    }

    private fun postUpdate() {
        protocol.postUpdate()
    }

    private fun tick() {
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i])
            player.updateCoord(0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            player.extendedInfo.setChat(
                0,
                0,
                0,
                false,
                "Neque porro quisquam est qui dolorem " +
                    "ipsum quia dolor sit amet, consectetur, adipisci velit",
                null,
            )
        }
        protocol.prepare()
        protocol.putBitcodes()
        protocol.prepareExtendedInfo()
        protocol.putExtendedInfo()
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i])
            player.backingBuffer().release()
        }
        postUpdate()
    }

    @Benchmark
    fun benchmark() {
        tick()
    }

    private companion object {
        private const val MAX_IDX: Int = 2047

        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = PlayerInfoBenchmark::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }
    }
}

fun main() {
    val bench = PlayerInfoBenchmark()
    bench.setup()
    val count = 1
    val time =
        measureTime {
            repeat(count) {
                bench.benchmark()
            }
        }
    println(time / count)
}
