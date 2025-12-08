package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
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
    private lateinit var infoProtocols: InfoProtocols
    private lateinit var infos: Infos

    @BeforeEach
    fun initialize() {
        this.infoProtocols = generateInfoProtocols()
        this.infos = infoProtocols.alloc(LOCAL_PLAYER_INDEX, OldSchoolClientType.DESKTOP)
        protocol = infoProtocols.playerInfoProtocol
        localPlayerInfo = infos.playerInfo
        updateCoord(0, 3200, 3220)
        localPlayerInfo.avatar.postUpdate()
        client = PlayerInfoClient()
        infos.updateRootBuildAreaCenteredOnPlayer(3200, 3220)
        gpiInit()
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
        val packet = localPlayerInfo.toPacket()
        packet.consume()
        val buffer = packet.content()
        client.decode(buffer)
        assertFalse(buffer.isReadable)
    }

    @Test
    fun `test single player consecutive movements`() {
        // For local player, the coord we send in init should always be the real, high resolution
        // As such, we must call tick() to for any future changes to take effect
        tick()

        updateCoord(1, 3210, 3225)
        tick()
        assertCoordEquals()

        updateCoord(0, 512, 512)
        tick()
        assertCoordEquals()

        updateCoord(0, 513, 512)
        tick()
        assertCoordEquals()

        updateCoord(0, 3205, 3220)
        tick()
        assertCoordEquals()
    }

    private fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        infos.updateRootCoord(level, x, z)
    }

    @Test
    fun `test multi player movements`() {
        val otherPlayerIndices = (1..280)
        val otherPlayers = arrayOfNulls<Infos>(2048)
        for (index in otherPlayerIndices) {
            val otherPlayer = infoProtocols.alloc(index, OldSchoolClientType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateRootCoord(0, 3205, 3220)
        }
        tick()
        assertAllCoordsEqual(otherPlayers)
        for (player in otherPlayers.filterNotNull()) {
            player.updateRootCoord(0, 3204, 3220)
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
        localPlayerInfo.avatar.extendedInfo.setBodyType(1)
        localPlayerInfo.avatar.extendedInfo.setPronoun(2)
        localPlayerInfo.avatar.extendedInfo.setSkullIcon(-1)
        localPlayerInfo.avatar.extendedInfo.setOverheadIcon(-1)
        tick()
        assertEquals("Local Player", clientLocalPlayer.name)
        assertEquals(126, clientLocalPlayer.combatLevel)
        assertEquals(1258, clientLocalPlayer.skillLevel)
        assertEquals(false, clientLocalPlayer.hidden)
        assertEquals(1, clientLocalPlayer.gender)
        assertEquals(2, clientLocalPlayer.textGender)
        assertEquals(-1, clientLocalPlayer.skullIcon)
        assertEquals(-1, clientLocalPlayer.headIcon)
    }

    @Test
    fun `test multi player appearance extended info`() {
        val otherPlayerIndices = (1..280)
        val otherPlayers = arrayOfNulls<Infos>(2048)
        for (index in otherPlayerIndices) {
            val otherPlayer = infoProtocols.alloc(index, OldSchoolClientType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateRootCoord(0, 3205, 3220)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setName("Player $index")
            otherPlayer.playerInfo.avatar.extendedInfo
                .setCombatLevel(126)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setSkillLevel(index)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setHidden(false)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setBodyType(1)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setPronoun(2)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setSkullIcon(-1)
            otherPlayer.playerInfo.avatar.extendedInfo
                .setOverheadIcon(-1)
        }
        tick()
        for (index in otherPlayerIndices) {
            val clientPlayer = client.cachedPlayers[index]
            assertNotNull(clientPlayer)
            assertEquals("Player $index", clientPlayer.name)
            assertEquals(126, clientPlayer.combatLevel)
            assertEquals(index, clientPlayer.skillLevel)
            assertEquals(false, clientPlayer.hidden)
            assertEquals(1, clientPlayer.gender)
            assertEquals(2, clientPlayer.textGender)
            assertEquals(-1, clientPlayer.skullIcon)
            assertEquals(-1, clientPlayer.headIcon)
        }
    }

    private fun assertAllCoordsEqual(otherPlayers: Array<Infos?>) {
        for (i in otherPlayers.indices) {
            val otherPlayer = otherPlayers[i] ?: continue
            val clientPlayer = client.cachedPlayers[i]!!
            assertEquals(otherPlayer.playerInfo.avatar.currentCoord, clientPlayer.coord)
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
