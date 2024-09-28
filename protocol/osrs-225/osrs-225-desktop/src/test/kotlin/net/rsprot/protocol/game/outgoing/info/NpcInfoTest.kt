package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.DeferredNpcInfoProtocolSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatar
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class NpcInfoTest {
    private lateinit var protocol: NpcInfoProtocol
    private lateinit var client: NpcInfoClient
    private val random: Random = Random(0)
    private lateinit var serverNpcs: List<Npc>
    private lateinit var localNpcInfo: NpcInfo
    private var localPlayerCoord = CoordGrid(0, 3207, 3207)
    private lateinit var factory: NpcAvatarFactory

    @BeforeEach
    fun initialize() {
        val allocator = PooledByteBufAllocator.DEFAULT
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
                zoneIndexStorage = storage,
            )
        protocolSupplier.supply(protocol)
        this.client = NpcInfoClient()
        this.localNpcInfo = protocol.alloc(500, OldSchoolClientType.DESKTOP)
    }

    private fun npcExceptionHandler(): NpcAvatarExceptionHandler =
        NpcAvatarExceptionHandler { _, e ->
            e.printStackTrace()
        }

    private fun tick() {
        localNpcInfo.updateCoord(NpcInfo.ROOT_WORLD, localPlayerCoord.level, localPlayerCoord.x, localPlayerCoord.z)
        localNpcInfo.updateBuildArea(
            NpcInfo.ROOT_WORLD,
            BuildArea(
                (localPlayerCoord.x ushr 3) - 6,
                (localPlayerCoord.z ushr 3) - 6,
            ),
        )
        protocol.update()
    }

    @Test
    fun `adding npcs to high resolution`() {
        this.serverNpcs = createPhantomNpcs(factory)
        tick()
        val buffer = this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD)
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
        this.serverNpcs = createPhantomNpcs(factory)
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)

        this.localPlayerCoord = CoordGrid(0, 2000, 2000)
        this.localNpcInfo.updateCoord(
            NpcInfo.ROOT_WORLD,
            localPlayerCoord.level,
            localPlayerCoord.x,
            localPlayerCoord.z,
        )
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(0, client.npcSlotCount)
    }

    @Test
    fun `single npc walking`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        val clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.walk(0, 1)
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
    }

    @Test
    fun `single npc crawling`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        val clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.crawl(0, 1)
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
    }

    @Test
    fun `single npc running`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        val clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.walk(0, 1)
        npc.avatar.walk(0, 1)
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
    }

    @Test
    fun `single npc telejumping`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        var clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.teleport(
            localPlayerCoord.level,
            localPlayerCoord.x + 10,
            localPlayerCoord.z + 10,
            true,
        )
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        // Re-obtain the instance as teleporting is equal to removal + adding
        clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
    }

    @Test
    fun `single npc teleporting`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        var clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.teleport(
            localPlayerCoord.level,
            localPlayerCoord.x + 10,
            localPlayerCoord.z + 10,
            false,
        )
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        // Re-obtain the instance as teleporting is equal to removal + adding
        clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
    }

    @Test
    fun `single npc overhead chat`() {
        serverNpcs = createSingleNpc(factory)
        // Skip everyone but the first entry
        val npc = serverNpcs.first()
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(1, client.npcSlotCount)
        val clientNpc = checkNotNull(client.cachedNpcs[client.npcSlot[0]])
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)

        npc.avatar.extendedInfo.setSay("Hello world")
        tick()
        client.decode(this.localNpcInfo.backingBuffer(NpcInfo.ROOT_WORLD), false, localPlayerCoord)
        assertEquals(npc.id, clientNpc.id)
        assertEquals(npc.index, clientNpc.index)
        assertEquals(npc.coordGrid, clientNpc.coord)
        assertEquals("Hello world", clientNpc.overheadChat)
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

    private fun createSingleNpc(factory: NpcAvatarFactory): List<Npc> {
        val npcs = ArrayList<Npc>(1)
        val x = random.nextInt(3200, 3213)
        val z = random.nextInt(3200, 3213)
        val coord = CoordGrid(0, x, z)
        npcs +=
            Npc(
                0,
                0,
                factory.alloc(
                    0,
                    0,
                    coord.level,
                    coord.x,
                    coord.z,
                ),
            )
        return npcs
    }

    private data class Npc(
        val index: Int,
        val id: Int,
        val avatar: NpcAvatar,
    ) {
        val coordGrid: CoordGrid
            get() = avatar.getCoordGrid()

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
