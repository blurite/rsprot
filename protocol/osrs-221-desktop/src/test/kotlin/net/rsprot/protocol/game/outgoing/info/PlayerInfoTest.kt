package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.shared.platform.PlatformType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlayerInfoTest {
    private lateinit var protocol: PlayerInfoProtocol
    private lateinit var localPlayerInfo: PlayerInfo
    private lateinit var client: PlayerInfoClient
    private lateinit var clientLocalPlayer: PlayerInfoClient.Companion.Player

    @BeforeEach
    fun initialize() {
        protocol =
            PlayerInfoProtocol(
                2048,
                PooledByteBufAllocator.DEFAULT,
                emptyMap(),
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
        client.decode(localPlayerInfo.backingBuffer())
        postUpdate()
    }

    @Test
    fun `test consecutive movements`() {
        localPlayerInfo.updateCoord(1, 3210, 3225)
        tick()
        assertCoordEquals()

        localPlayerInfo.updateCoord(0, 0, 0)
        tick()
        assertCoordEquals()

        localPlayerInfo.updateCoord(0, 1, 0)
        tick()
        assertCoordEquals()
    }

    @Test
    fun `test multiplayer movements`() {
        val otherPlayerIndices = (1..280)
        val otherPlayers = arrayOfNulls<PlayerInfo>(2048)
        for (index in otherPlayerIndices) {
            val otherPlayer = protocol.alloc(index, PlatformType.DESKTOP)
            otherPlayers[index] = otherPlayer
            otherPlayer.updateCoord(0, 3205, 3220)
        }
        tick()
        assertCoordEquals()
        for (player in otherPlayers.filterNotNull()) {
            player.updateCoord(0, 3204, 3220)
        }
        tick()
        assertCoordEquals()
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
