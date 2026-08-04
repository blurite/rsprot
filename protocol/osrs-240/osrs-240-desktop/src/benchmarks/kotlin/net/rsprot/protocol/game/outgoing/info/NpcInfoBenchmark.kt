package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatar
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoLargeV6
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoSmallV6
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
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
class NpcInfoBenchmark {
    init {
        System.setProperty("net.rsprot.protocol.internal.npcPlayerAvatarTracking", "true")
    }

    private lateinit var protocols: InfoProtocols
    private val random: Random = Random(0)
    private lateinit var serverNpcs: List<Npc>
    private lateinit var infos: List<Infos>
    private var localPlayerCoord = CoordGrid(0, 3207, 3207)
    private lateinit var factory: NpcAvatarFactory

    @Setup
    fun setup() {
        val context =
            generateBenchmarkInfoProtocols(
                npcProtocolWorker = DefaultProtocolWorker(1, ForkJoinPool.commonPool()),
            )
        this.protocols = context.protocols
        this.factory = context.npcAvatarFactory
        this.serverNpcs = createPhantomNpcs(factory)
        this.infos =
            (1..2046).map { index ->
                protocols.alloc(index, OldSchoolClientType.DESKTOP).also { infos ->
                    infos.updateRootCoord(
                        localPlayerCoord.level,
                        localPlayerCoord.x,
                        localPlayerCoord.z,
                    )
                    infos.updateRootBuildAreaCenteredOnPlayer(
                        localPlayerCoord.x,
                        localPlayerCoord.z,
                    )
                }
            }
    }

    @Benchmark
    fun benchmark() {
        tick()
    }

    private fun tick() {
        for (npc in serverNpcs) {
            npc.avatar.extendedInfo.setSay("Neque porro quisquam est qui dolorem ipsum quia do")
            npc.avatar.teleport(
                0,
                random.nextInt(3200, 3213),
                random.nextInt(3200, 3213),
                true,
            )
        }
        protocols.npcInfoProtocol.update()
        for (infos in infos) {
            val packet = checkNotNull(infos.npcInfo.internalPacketResult(NpcInfo.ROOT_WORLD).getOrNull())
            packet.markConsumed()
            when (packet) {
                is NpcInfoSmallV6 -> packet.release()
                is NpcInfoLargeV6 -> packet.release()
                else -> throw IllegalStateException("Unknown packet type: $packet")
            }
        }
        for (npc in serverNpcs) {
            npc.avatar.postUpdate()
        }
    }

    private fun createPhantomNpcs(factory: NpcAvatarFactory): List<Npc> {
        val npcs = ArrayList<Npc>(500)
        for (index in 0..<500) {
            val x = random.nextInt(3200, 3213)
            val z = random.nextInt(3200, 3213)
            val id = (index * x * z) and 0x3FFF
            val coord = CoordGrid(0, x, z)
            npcs +=
                Npc(
                    index,
                    id,
                    factory.alloc(
                        index,
                        id,
                        coord.level,
                        coord.x,
                        coord.z,
                    ),
                )
        }
        return npcs
    }

    private data class Npc(
        val index: Int,
        val id: Int,
        val avatar: NpcAvatar,
    ) {
        override fun toString(): String =
            "Npc(" +
                "index=$index, " +
                "id=$id, " +
                "coordGrid=${avatar.getCoordGrid()}" +
                ")"
    }

    private companion object {
        private fun NpcAvatar.getCoordGrid(): CoordGrid = CoordGrid(level(), x(), z())
    }
}
