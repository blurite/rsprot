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
import org.openjdk.jmh.annotations.Param
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
    private lateinit var positions: Array<PlayerPosition?>
    private val random: Random = Random(0)
    private var tickCycle: Int = 0

    @Param
    private lateinit var scenario: Scenario

    @Param
    private lateinit var activity: Activity

    @Setup
    fun setup() {
        val context =
            generateBenchmarkInfoProtocols(
                playerProtocolWorker = DefaultProtocolWorker(Int.MAX_VALUE, ForkJoinPool.commonPool()),
            )
        protocols = context.protocols
        players = arrayOfNulls(PROTOCOL_CAPACITY)
        positions = arrayOfNulls(PROTOCOL_CAPACITY)
        if (scenario == Scenario.MIXED_WORLD_ENTITIES) {
            repeat(WORLD_ENTITY_COUNT) { index ->
                context.worldEntityAvatarFactory.alloc(
                    index = index + 1,
                    id = index,
                    ownerIndex = 0,
                    sizeX = 1,
                    sizeZ = 1,
                    southWestZoneX = INSTANCE_ZONE_X + index,
                    southWestZoneZ = INSTANCE_ZONE_Z,
                    minLevel = 0,
                    maxLevel = 0,
                    fineX = (ROOT_X + index * 2) * 128 + 64,
                    fineZ = ROOT_Z * 128 + 64,
                    projectedLevel = 0,
                    activeLevel = 0,
                    angle = 0,
                )
            }
        }
        for (i in 1..<MAX_IDX) {
            val infos = protocols.alloc(i, OldSchoolClientType.DESKTOP)
            players[i] = infos
            val position = position(i)
            positions[i] = position
            updateCoord(infos, position)
            infos.playerInfo.avatar.setPreferredPlayerCountLimit(MAX_IDX)
            infos.playerInfo.avatar.postUpdate()
            initializeAppearance(infos.playerInfo, i)
        }
        if (scenario == Scenario.MIXED_WORLD_ENTITIES) {
            protocols.worldEntityInfoProtocol.update()
            for (i in 1..<MAX_IDX) {
                val infos = checkNotNull(players[i])
                val result = infos.getPackets().rootWorldInfoPackets.worldEntityInfo
                val packet = checkNotNull(result.getOrNull())
                packet.consume()
                packet.release()
            }
        }
    }

    private fun updateCoord(
        infos: Infos,
        position: PlayerPosition,
    ) {
        infos.updateRootCoord(position.level, position.x, position.z)
        infos.updateRootBuildAreaCenteredOnPlayer(position.buildAreaCenterX, position.buildAreaCenterZ)
    }

    private fun position(index: Int): PlayerPosition =
        when (scenario) {
            Scenario.DENSE_ROOT ->
                PlayerPosition(
                    level = 0,
                    x = ROOT_X + random.nextInt(8),
                    z = ROOT_Z + random.nextInt(8),
                    buildAreaCenterX = ROOT_X,
                    buildAreaCenterZ = ROOT_Z,
                )
            Scenario.DISTRIBUTED_ROOT -> {
                val x = ROOT_X + (index % 50) * 32
                val z = ROOT_Z + (index / 50) * 32
                PlayerPosition(0, x, z, x, z)
            }
            Scenario.MIXED_WORLD_ENTITIES -> {
                if (index <= ROOT_PLAYER_COUNT) {
                    PlayerPosition(0, ROOT_X + index % 8, ROOT_Z + index / 8 % 8, ROOT_X, ROOT_Z)
                } else {
                    val worldIndex = (index - ROOT_PLAYER_COUNT - 1) % WORLD_ENTITY_COUNT
                    PlayerPosition(
                        level = 0,
                        x = (INSTANCE_ZONE_X + worldIndex) * 8 + index % 8,
                        z = INSTANCE_ZONE_Z * 8 + index / 8 % 8,
                        buildAreaCenterX = ROOT_X + worldIndex * 2,
                        buildAreaCenterZ = ROOT_Z,
                    )
                }
            }
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
            val position = checkNotNull(positions[i])
            val x =
                if (activity == Activity.ALTERNATING_HALF && i and 1 == 0) {
                    position.x xor (tickCycle and 1)
                } else {
                    position.x
                }
            infos.updateRootCoord(position.level, x, position.z)
            infos.updateRootBuildAreaCenteredOnPlayer(position.buildAreaCenterX, position.buildAreaCenterZ)
        }
        tickCycle++
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
        private const val MAX_IDX: Int = 2001
        private const val ROOT_PLAYER_COUNT: Int = 1000
        private const val WORLD_ENTITY_COUNT: Int = 4
        private const val ROOT_X: Int = 3200
        private const val ROOT_Z: Int = 3200
        private const val INSTANCE_ZONE_X: Int = 800
        private const val INSTANCE_ZONE_Z: Int = 800
    }

    private data class PlayerPosition(
        val level: Int,
        val x: Int,
        val z: Int,
        val buildAreaCenterX: Int,
        val buildAreaCenterZ: Int,
    )

    enum class Scenario {
        DENSE_ROOT,
        DISTRIBUTED_ROOT,
        MIXED_WORLD_ENTITIES,
    }

    enum class Activity {
        STATIONARY,
        ALTERNATING_HALF,
    }
}
