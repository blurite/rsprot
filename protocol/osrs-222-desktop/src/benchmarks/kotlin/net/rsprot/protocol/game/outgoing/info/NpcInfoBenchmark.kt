@file:Suppress("OPT_IN_USAGE")

package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatar
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcIndexSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
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
class NpcInfoBenchmark {
    private lateinit var protocol: NpcInfoProtocol
    private val random: Random = Random(0)
    private lateinit var serverNpcs: List<Npc>
    private lateinit var supplier: NpcIndexSupplier
    private lateinit var localNpcInfo: NpcInfo
    private lateinit var otherNpcInfos: List<NpcInfo>
    private var localPlayerCoord = CoordGrid(0, 3207, 3207)

    @Setup
    fun setup() {
        val allocator = PooledByteBufAllocator.DEFAULT
        val factory =
            NpcAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(NpcAvatarExtendedInfoDesktopWriter()),
                DefaultHuffmanCodecProvider(createHuffmanCodec()),
            )
        this.serverNpcs = createPhantomNpcs(factory)
        this.supplier = createNpcIndexSupplier()

        val encoders =
            ClientTypeMap.of(
                listOf(DesktopLowResolutionChangeEncoder()),
                OldSchoolClientType.COUNT,
            ) {
                it.clientType
            }
        protocol =
            NpcInfoProtocol(
                allocator,
                supplier,
                encoders,
                factory,
                npcExceptionHandler(),
                DefaultProtocolWorker(1, ForkJoinPool.commonPool()),
            )
        this.localNpcInfo = protocol.alloc(1, OldSchoolClientType.DESKTOP)
        otherNpcInfos = (2..2046).map { protocol.alloc(it, OldSchoolClientType.DESKTOP) }
        val infos = otherNpcInfos + localNpcInfo
        for (info in infos) {
            info.updateCoord(NpcInfo.ROOT_WORLD, localPlayerCoord.level, localPlayerCoord.x, localPlayerCoord.z)
        }
    }

    private fun npcExceptionHandler(): NpcAvatarExceptionHandler {
        return NpcAvatarExceptionHandler { _, _ ->
            // No-op
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
        protocol.update()
        for (i in 1..2046) {
            val info = protocol[i]
            info.backingBuffer(NpcInfo.ROOT_WORLD).release()
        }
        for (npc in serverNpcs) {
            npc.avatar.postUpdate()
        }
    }

    private fun createNpcIndexSupplier(): NpcIndexSupplier {
        return NpcIndexSupplier { _, level, x, z, viewDistance ->
            serverNpcs
                .asSequence()
                .filter { it.coordGrid.inDistance(CoordGrid(level, x, z), viewDistance) }
                .take(250)
                .mapTo(ArrayList(250)) { it.index }
                .iterator()
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
        val coordGrid: CoordGrid
            get() = avatar.getCoordGrid()

        override fun toString(): String {
            return "Npc(" +
                "index=$index, " +
                "id=$id, " +
                "coordGrid=${avatar.getCoordGrid()}" +
                ")"
        }
    }

    private companion object {
        private fun NpcAvatar.getCoordGrid(): CoordGrid {
            return CoordGrid(level(), x(), z())
        }

        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = PlayerInfoTest::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }
    }
}
