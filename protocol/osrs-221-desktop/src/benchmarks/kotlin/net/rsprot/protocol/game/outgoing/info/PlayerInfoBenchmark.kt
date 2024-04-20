package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
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
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import kotlin.random.Random

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
        val allocator = PooledByteBufAllocator.DEFAULT
        val factory =
            PlayerAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(PlayerAvatarExtendedInfoDesktopWriter()),
                createHuffmanCodec(),
            )
        protocol =
            PlayerInfoProtocol(
                allocator,
                DefaultProtocolWorker(Int.MAX_VALUE, ForkJoinPool.commonPool()),
                factory,
            )
        players = arrayOfNulls(PROTOCOL_CAPACITY)
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
        player.avatar.extendedInfo.setName("Bot $index")
        player.avatar.extendedInfo.setCombatLevel(126)
        player.avatar.extendedInfo.setSkillLevel(0)
        player.avatar.extendedInfo.setHidden(false)
        player.avatar.extendedInfo.setMale(true)
        player.avatar.extendedInfo.setTextGender(0)
        player.avatar.extendedInfo.setSkullIcon(-1)
        player.avatar.extendedInfo.setOverheadIcon(-1)

        for (colIdx in 0..<5) {
            player.avatar.extendedInfo.setColour(colIdx, colIdx * 10)
        }
        player.avatar.extendedInfo.setIdentKit(8, 0)
        player.avatar.extendedInfo.setIdentKit(11, 10)
        player.avatar.extendedInfo.setIdentKit(4, 18)
        player.avatar.extendedInfo.setIdentKit(6, 26)
        player.avatar.extendedInfo.setIdentKit(9, 33)
        player.avatar.extendedInfo.setIdentKit(7, 36)
        player.avatar.extendedInfo.setIdentKit(10, 42)
        player.avatar.extendedInfo.setBaseAnimationSet(
            808,
            823,
            819,
            820,
            821,
            822,
            824,
        )
    }

    private fun postUpdate() {
        protocol.postUpdate()
    }

    private fun tick() {
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i])
            player.updateCoord(0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            player.avatar.extendedInfo.setChat(
                0,
                0,
                0,
                false,
                "Neque porro quisquam est qui dolorem ipsum quia do",
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
