package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.shared.platform.PlatformType
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
        val writers = listOf(PlayerAvatarExtendedInfoDesktopWriter())
        protocol =
            PlayerInfoProtocol(
                PooledByteBufAllocator.DEFAULT,
                DefaultProtocolWorker(),
                DefaultExtendedInfoFilter(),
                writers,
                createHuffmanCodec(),
            )
        localPlayerInfo = protocol.alloc(LOCAL_PLAYER_INDEX, PlatformType.DESKTOP)
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
        protocol.prepare()
        protocol.putBitcodes()
        protocol.prepareExtendedInfo()
        protocol.putExtendedInfo()
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
            val otherPlayer = protocol.alloc(index, PlatformType.DESKTOP)
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
        localPlayerInfo.extendedInfo.setName("Local Player")
        localPlayerInfo.extendedInfo.setCombatLevel(126)
        localPlayerInfo.extendedInfo.setSkillLevel(1258)
        localPlayerInfo.extendedInfo.setHidden(false)
        localPlayerInfo.extendedInfo.setMale(false)
        localPlayerInfo.extendedInfo.setTextGender(2)
        localPlayerInfo.extendedInfo.setSkullIcon(-1)
        localPlayerInfo.extendedInfo.setOverheadIcon(-1)
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
            val otherPlayer = protocol.alloc(index, PlatformType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateCoord(0, 3205, 3220)
            otherPlayer.extendedInfo.setName("Player $index")
            otherPlayer.extendedInfo.setCombatLevel(126)
            otherPlayer.extendedInfo.setSkillLevel(index)
            otherPlayer.extendedInfo.setHidden(false)
            otherPlayer.extendedInfo.setMale(false)
            otherPlayer.extendedInfo.setTextGender(2)
            otherPlayer.extendedInfo.setSkullIcon(-1)
            otherPlayer.extendedInfo.setOverheadIcon(-1)
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
