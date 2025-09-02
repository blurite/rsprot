package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.Unpooled
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
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
        val allocator = UnpooledByteBufAllocator.DEFAULT
        val factory =
            PlayerAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(PlayerAvatarExtendedInfoDesktopWriter()),
                DefaultHuffmanCodecProvider(createHuffmanCodec()),
            )
        protocol =
            PlayerInfoProtocol(
                allocator,
                DefaultProtocolWorker(Int.MAX_VALUE, ForkJoinPool.commonPool()),
                factory,
            )
        players = arrayOfNulls(PROTOCOL_CAPACITY)
        for (i in 1..<MAX_IDX) {
            val player = protocol.alloc(i, OldSchoolClientType.DESKTOP)
            players[i] = player
            updateCoord(player, 0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            player.avatar.postUpdate()
            initializeAppearance(player, i)
        }
    }

    private fun updateCoord(
        player: PlayerInfo,
        @Suppress("SameParameterValue") level: Int,
        x: Int,
        z: Int,
    ) {
        player.updateCoord(level, x, z)
        player.updateRenderCoord(PlayerInfo.ROOT_WORLD, level, x, z)
        player.updateBuildArea(PlayerInfo.ROOT_WORLD, BuildArea((x ushr 3) - 6, (z ushr 3) - 6))
    }

    private fun initializeAppearance(
        player: PlayerInfo,
        index: Int,
    ) {
        player.avatar.extendedInfo.setName("Bot $index")
        player.avatar.extendedInfo.setCombatLevel(126)
        player.avatar.extendedInfo.setSkillLevel(0)
        player.avatar.extendedInfo.setHidden(false)
        player.avatar.extendedInfo.setBodyType(0)
        player.avatar.extendedInfo.setPronoun(0)
        player.avatar.extendedInfo.setSkullIcon(-1)
        player.avatar.extendedInfo.setOverheadIcon(-1)

        for (colIdx in 0..<5) {
            player.avatar.extendedInfo.setColour(colIdx, colIdx * 10)
        }
        player.avatar.extendedInfo.setIdentKit(0, 0)
        player.avatar.extendedInfo.setIdentKit(1, 10)
        player.avatar.extendedInfo.setIdentKit(2, 18)
        player.avatar.extendedInfo.setIdentKit(3, 26)
        player.avatar.extendedInfo.setIdentKit(4, 33)
        player.avatar.extendedInfo.setIdentKit(5, 36)
        player.avatar.extendedInfo.setIdentKit(6, 42)
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

    private fun tick() {
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i])
            updateCoord(player, 0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            player.avatar.extendedInfo.setChat(
                0,
                0,
                0,
                false,
                "Neque porro quisquam est qui dolorem ipsum quia do",
                null,
            )
        }
        protocol.update()
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i])
            val packet = player.toPacket(PlayerInfo.ROOT_WORLD)
            packet.release()
        }
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
