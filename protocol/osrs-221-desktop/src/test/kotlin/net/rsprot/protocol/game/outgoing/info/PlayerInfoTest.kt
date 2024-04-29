package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class PlayerInfoTest {
    private lateinit var protocol: PlayerInfoProtocol
    private lateinit var localPlayerInfo: PlayerInfo
    private lateinit var client: PlayerInfoClient
    private lateinit var clientLocalPlayer: PlayerInfoClient.Companion.Player

    @BeforeEach
    fun initialize() {
        val allocator = PooledByteBufAllocator.DEFAULT
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
                DefaultProtocolWorker(),
                factory,
            )
        localPlayerInfo = protocol.alloc(LOCAL_PLAYER_INDEX, OldSchoolClientType.DESKTOP)
        localPlayerInfo.updateCoord(0, 3200, 3220)
        client = PlayerInfoClient()
        gpiInit()
        postUpdate()
    }

    private fun postUpdate() {
        protocol.postUpdate()
    }

    private fun gpiInit() {
        val gpiBuffer = Unpooled.buffer(5000)
        localPlayerInfo.handleAbsolutePlayerPositions(gpiBuffer)
        client.gpiInit(LOCAL_PLAYER_INDEX, gpiBuffer)
        clientLocalPlayer = checkNotNull(client.cachedPlayers[client.localIndex])
    }

    @Test
    fun `test gpi init`() {
        assertCoordEquals()
    }

    private fun tick() {
        protocol.update()
        val buffer = localPlayerInfo.backingBuffer()
        client.decode(buffer)
        assertFalse(buffer.isReadable)
        postUpdate()
    }

    @Test
    fun `test single player consecutive movements`() {
        localPlayerInfo.updateCoord(1, 3210, 3225)
        tick()
        assertCoordEquals()

        localPlayerInfo.updateCoord(0, 0, 0)
        tick()
        assertCoordEquals()

        localPlayerInfo.updateCoord(0, 1, 0)
        tick()
        assertCoordEquals()

        localPlayerInfo.updateCoord(0, 3205, 3220)
        tick()
        assertCoordEquals()
    }

    @Test
    fun `test multi player movements`() {
        val otherPlayerIndices = (1..280)
        val otherPlayers = arrayOfNulls<PlayerInfo>(2048)
        for (index in otherPlayerIndices) {
            val otherPlayer = protocol.alloc(index, OldSchoolClientType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateCoord(0, 3205, 3220)
        }
        tick()
        assertAllCoordsEqual(otherPlayers)
        for (player in otherPlayers.filterNotNull()) {
            player.updateCoord(0, 3204, 3220)
        }
        tick()
        assertAllCoordsEqual(otherPlayers)
    }

    @Test
    fun `test single player appearance extended info`() {
        localPlayerInfo.avatar.extendedInfo.setName("Local Player")
        localPlayerInfo.avatar.extendedInfo.setCombatLevel(126)
        localPlayerInfo.avatar.extendedInfo.setSkillLevel(1258)
        localPlayerInfo.avatar.extendedInfo.setHidden(false)
        localPlayerInfo.avatar.extendedInfo.setMale(false)
        localPlayerInfo.avatar.extendedInfo.setTextGender(2)
        localPlayerInfo.avatar.extendedInfo.setSkullIcon(-1)
        localPlayerInfo.avatar.extendedInfo.setOverheadIcon(-1)
        tick()
        assertEquals("Local Player", clientLocalPlayer.name)
        assertEquals(126, clientLocalPlayer.combatLevel)
        assertEquals(1258, clientLocalPlayer.skillLevel)
        assertEquals(false, clientLocalPlayer.hidden)
        assertEquals(0, clientLocalPlayer.gender)
        assertEquals(2, clientLocalPlayer.textGender)
        assertEquals(-1, clientLocalPlayer.skullIcon)
        assertEquals(-1, clientLocalPlayer.headIcon)
    }

    @Test
    fun `test multi player appearance extended info`() {
        val otherPlayerIndices = (1..280)
        val otherPlayers = arrayOfNulls<PlayerInfo>(2048)
        for (index in otherPlayerIndices) {
            val otherPlayer = protocol.alloc(index, OldSchoolClientType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateCoord(0, 3205, 3220)
            otherPlayer.avatar.extendedInfo.setName("Player $index")
            otherPlayer.avatar.extendedInfo.setCombatLevel(126)
            otherPlayer.avatar.extendedInfo.setSkillLevel(index)
            otherPlayer.avatar.extendedInfo.setHidden(false)
            otherPlayer.avatar.extendedInfo.setMale(false)
            otherPlayer.avatar.extendedInfo.setTextGender(2)
            otherPlayer.avatar.extendedInfo.setSkullIcon(-1)
            otherPlayer.avatar.extendedInfo.setOverheadIcon(-1)
        }
        tick()
        for (index in otherPlayerIndices) {
            val clientPlayer = client.cachedPlayers[index]
            assertNotNull(clientPlayer)
            assertEquals("Player $index", clientPlayer.name)
            assertEquals(126, clientPlayer.combatLevel)
            assertEquals(index, clientPlayer.skillLevel)
            assertEquals(false, clientPlayer.hidden)
            assertEquals(0, clientPlayer.gender)
            assertEquals(2, clientPlayer.textGender)
            assertEquals(-1, clientPlayer.skullIcon)
            assertEquals(-1, clientPlayer.headIcon)
        }
    }

    private fun assertAllCoordsEqual(otherPlayers: Array<PlayerInfo?>) {
        for (i in otherPlayers.indices) {
            val otherPlayer = otherPlayers[i] ?: continue
            val clientPlayer = client.cachedPlayers[i]!!
            assertEquals(otherPlayer.avatar.currentCoord, clientPlayer.coord)
        }
    }

    private fun assertCoordEquals() {
        assertEquals(localPlayerInfo.avatar.currentCoord, clientLocalPlayer.coord)
    }

    private companion object {
        private const val LOCAL_PLAYER_INDEX: Int = 499

        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = PlayerInfoTest::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }
    }
}
