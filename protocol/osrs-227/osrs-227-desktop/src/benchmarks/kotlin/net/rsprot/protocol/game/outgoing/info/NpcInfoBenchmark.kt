package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.Unpooled
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.DeferredNpcInfoProtocolSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatar
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoLargeV5
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoSmallV5
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage
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

    private lateinit var protocol: NpcInfoProtocol
    private val random: Random = Random(0)
    private lateinit var serverNpcs: List<Npc>
    private lateinit var localNpcInfo: NpcInfo
    private lateinit var otherNpcInfos: List<NpcInfo>
    private var localPlayerCoord = CoordGrid(0, 3207, 3207)
    private lateinit var factory: NpcAvatarFactory

    @Setup
    fun setup() {
        val allocator = UnpooledByteBufAllocator.DEFAULT
        val storage = ZoneIndexStorage(ZoneIndexStorage.NPC_CAPACITY)
        val protocolSupplier = DeferredNpcInfoProtocolSupplier()
        this.factory =
            NpcAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(NpcAvatarExtendedInfoDesktopWriter()),
                DefaultHuffmanCodecProvider(createHuffmanCodec()),
                storage,
                protocolSupplier,
            )
        this.serverNpcs = createPhantomNpcs(factory)

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
                encoders,
                factory,
                npcExceptionHandler(),
                DefaultProtocolWorker(1, ForkJoinPool.commonPool()),
                storage,
            )
        protocolSupplier.supply(protocol)
        this.localNpcInfo = protocol.alloc(1, OldSchoolClientType.DESKTOP)
        otherNpcInfos = (2..2046).map { protocol.alloc(it, OldSchoolClientType.DESKTOP) }
        val infos = otherNpcInfos + localNpcInfo
        for (info in infos) {
            info.updateCoord(NpcInfo.ROOT_WORLD, localPlayerCoord.level, localPlayerCoord.x, localPlayerCoord.z)
            info.updateBuildArea(
                NpcInfo.ROOT_WORLD,
                BuildArea(
                    (localPlayerCoord.x ushr 3) - 6,
                    (localPlayerCoord.z ushr 3) - 6,
                ),
            )
        }
    }

    private fun npcExceptionHandler(): NpcAvatarExceptionHandler =
        NpcAvatarExceptionHandler { _, e ->
            e.printStackTrace()
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
            val packet = info.toPacket(NpcInfo.ROOT_WORLD)
            packet.markConsumed()
            when (packet) {
                is NpcInfoSmallV5 -> packet.release()
                is NpcInfoLargeV5 -> packet.release()
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

        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = PlayerInfoTest::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }
    }
}
