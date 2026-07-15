package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
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
    private lateinit var protocols: InfoProtocols
    private lateinit var players: Array<Infos?>
    private val random: Random = Random(0)

    @Setup
    fun setup() {
        protocols =
            generateBenchmarkInfoProtocols(
                playerProtocolWorker = DefaultProtocolWorker(Int.MAX_VALUE, ForkJoinPool.commonPool()),
            ).protocols
        players = arrayOfNulls(PROTOCOL_CAPACITY)
        for (i in 1..<MAX_IDX) {
            val infos = protocols.alloc(i, OldSchoolClientType.DESKTOP)
            players[i] = infos
            updateCoord(infos, 0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            infos.playerInfo.avatar.postUpdate()
            initializeAppearance(infos.playerInfo, i)
        }
    }

    private fun updateCoord(
        infos: Infos,
        @Suppress("SameParameterValue") level: Int,
        x: Int,
        z: Int,
    ) {
        infos.updateRootCoord(level, x, z)
        infos.updateRootBuildAreaCenteredOnPlayer(x, z)
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
            val infos = checkNotNull(players[i])
            updateCoord(infos, 0, random.nextInt(3200, 3213), random.nextInt(3200, 3213))
            val player = infos.playerInfo
            player.avatar.extendedInfo.setChat(
                0,
                0,
                0,
                false,
                "Neque porro quisquam est qui dolorem ipsum quia do",
                null,
            )
        }
        protocols.playerInfoProtocol.update()
        for (i in 1..<MAX_IDX) {
            val player = checkNotNull(players[i]).playerInfo
            val packet = checkNotNull(player.internalPacketResult().getOrNull())
            packet.consume()
            packet.release()
        }
    }

    @Benchmark
    fun benchmark() {
        tick()
    }

    private companion object {
        private const val MAX_IDX: Int = 2047
    }
}
