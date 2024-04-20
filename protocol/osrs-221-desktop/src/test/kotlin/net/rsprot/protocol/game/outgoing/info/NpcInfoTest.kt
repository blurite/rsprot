package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatar
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcIndexSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.platform.PlatformMap
import net.rsprot.protocol.shared.platform.PlatformType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

@Suppress("OPT_IN_USAGE")
class NpcInfoTest {
    private lateinit var protocol: NpcInfoProtocol
    private lateinit var client: NpcInfoClient
    private val random: Random = Random(0)
    private lateinit var serverNpcs: List<Npc>
    private lateinit var supplier: NpcIndexSupplier
    private lateinit var localNpcInfo: NpcInfo
    private var localPlayerCoord = CoordGrid(0, 3207, 3207)

    @OptIn(ExperimentalUnsignedTypes::class)
    @BeforeEach
    fun initialize() {
        val allocator = PooledByteBufAllocator.DEFAULT
        val factory =
            NpcAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(NpcAvatarExtendedInfoDesktopWriter()),
                createHuffmanCodec(),
            )
        this.serverNpcs = createPhantomNpcs(factory)
        this.supplier = createNpcIndexSupplier()

        val encoders =
            PlatformMap.of(
                listOf(DesktopLowResolutionChangeEncoder()),
                PlatformType.COUNT,
            ) {
                it.platform
            }
        protocol = NpcInfoProtocol(allocator, supplier, encoders, factory)
        this.client = NpcInfoClient()
        this.localNpcInfo = protocol.alloc(500, PlatformType.DESKTOP)
    }

    private fun tick() {
        localNpcInfo.updateCoord(localPlayerCoord.level, localPlayerCoord.x, localPlayerCoord.z)
        protocol.compute()
    }

    @Test
    fun `adding npcs to high resolution`() {
        tick()
        val buffer = this.localNpcInfo.backingBuffer()
        client.decode(buffer, false, localPlayerCoord)
        for (index in client.cachedNpcs.indices) {
            val clientNpc = client.cachedNpcs[index] ?: continue
            val serverNpc = this.serverNpcs[index]
            assertEquals(serverNpc.coordGrid, clientNpc.coord)
            assertEquals(serverNpc.index, clientNpc.index)
            assertEquals(serverNpc.id, clientNpc.id)
        }
    }

    @Test
    fun `removing npcs from high resolution`() {
        tick()
        client.decode(this.localNpcInfo.backingBuffer(), false, localPlayerCoord)

        this.localPlayerCoord = CoordGrid(0, 2000, 2000)
        this.localNpcInfo.updateCoord(localPlayerCoord.level, localPlayerCoord.x, localPlayerCoord.z)
        tick()
        client.decode(this.localNpcInfo.backingBuffer(), false, localPlayerCoord)
        assertEquals(0, client.npcSlotCount)
    }

    private fun createNpcIndexSupplier(): NpcIndexSupplier {
        return NpcIndexSupplier { _, level, x, z, viewDistance ->
            val grid = CoordGrid(level, x, z)
            val sequence =
                sequence {
                    for (npc in serverNpcs) {
                        if (npc.coordGrid.inDistance(grid, viewDistance)) {
                            yield(npc.index)
                        }
                    }
                }
            // Consume the sequence by calling toList()
            sequence.toList().iterator()
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
                    coord,
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
        val coordGrid: CoordGrid,
        val avatar: NpcAvatar,
    ) {
        override fun toString(): String {
            return "Npc(" +
                "index=$index, " +
                "id=$id, " +
                "coordGrid=$coordGrid" +
                ")"
        }
    }

    private companion object {
        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = PlayerInfoTest::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }
    }
}
