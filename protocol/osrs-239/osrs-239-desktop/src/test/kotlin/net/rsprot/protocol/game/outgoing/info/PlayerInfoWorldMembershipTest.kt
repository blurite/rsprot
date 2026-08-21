package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.Unpooled
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.worker.ForkJoinMultiThreadProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatar
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarFactory
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerInfoWorldMembershipTest {
    @Test
    fun `player visibility follows movement allocation removal active levels resize and priority`() {
        val fixture = PlayerFixture()

        fixture.updateObserver(0, ROOT_X, ROOT_Z)
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.updateTarget(0, ROOT_X + 16, ROOT_Z)
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        val world = fixture.allocWorld(1, INSTANCE_ZONE_X, activeLevel = 0, ROOT_X + 4)
        fixture.updateTarget(instanceCoord(0, 1))
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.updateTarget(instanceCoord(1, 1))
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.updateObserver(instanceCoord(1, 0))
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.allocWorld(2, INSTANCE_ZONE_X + 1, activeLevel = 1, ROOT_X + 8)
        fixture.updateTarget(instanceCoord(1, 1, INSTANCE_ZONE_X + 1))
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.updateTarget(0, ROOT_X + 5, ROOT_Z)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.target.playerInfo.avatar.hidden = true
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())
        fixture.target.playerInfo.avatar.hidden = false

        fixture.releaseWorld(world)
        fixture.updateObserver(0, ROOT_X, ROOT_Z)
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.observer.updateRootBuildArea(ROOT_X ushr 3, ROOT_Z ushr 3, 1, 1)
        fixture.updateTarget(0, ROOT_X + 8, ROOT_Z)
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.observer.updateRootBuildAreaCenteredOnPlayer(ROOT_X, ROOT_Z)
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.reallocateTarget()
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.observer.playerInfo.avatar
            .forceResizeRange(0)
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.target.playerInfo.avatar
            .setHighPriority()
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())
    }

    @Test
    fun `player info packet bytes match baseline across membership transitions`() {
        assertEquals(EXPECTED_PACKET_HEX, membershipTransitionPackets())
    }

    @Test
    fun `stationary snapshots preserve baseline packet bytes`() {
        val fixture = StationarySnapshotFixture()
        val packets = ArrayList<String>()

        fixture.updateTarget(10, near = true)
        fixture.updateTarget(11, near = true)
        fixture.updateTarget(20, near = false)
        fixture.updateTarget(21, near = false)
        packets += fixture.tick()

        fixture.updateTarget(10, near = true, xOffset = 2)
        fixture.updateTarget(11, near = true)
        fixture.updateTarget(20, near = false, xOffset = 2)
        fixture.updateTarget(21, near = false)
        packets += fixture.tick()

        fixture.updateTarget(10, near = false)
        fixture.updateTarget(11, near = false)
        fixture.updateTarget(20, near = true)
        fixture.updateTarget(21, near = true)
        packets += fixture.tick()

        assertEquals(EXPECTED_STATIONARY_SNAPSHOT_PACKET_HEX, packets)
    }

    @Test
    fun `stationary player membership is rebuilt after world entity allocation removal and index reuse`() {
        val fixture = PlayerFixture()

        fixture.updateObserver(0, ROOT_X + 7, ROOT_Z)
        fixture.updateTarget(0, ROOT_X + 8, ROOT_Z)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        val coveringWorld =
            fixture.allocWorld(
                index = 1,
                zoneX = (ROOT_X + 8) ushr 3,
                activeLevel = 0,
                projectedX = ROOT_X + 104,
                zoneZ = ROOT_Z ushr 3,
            )
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.releaseWorld(coveringWorld)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.allocWorld(
            index = 1,
            zoneX = INSTANCE_ZONE_X,
            activeLevel = 0,
            projectedX = ROOT_X + 104,
        )
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())
    }

    @Test
    fun `parallel observer updates retain baseline packet bytes`() {
        repeat(10) {
            assertEquals(
                EXPECTED_PACKET_HEX,
                membershipTransitionPackets(ForkJoinMultiThreadProtocolWorker()),
            )
        }
    }

    private fun membershipTransitionPackets(playerProtocolWorker: ProtocolWorker? = null): List<String> {
        val fixture = PlayerFixture(playerProtocolWorker)
        val packets = ArrayList<String>()

        fixture.updateObserver(0, ROOT_X, ROOT_Z)
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        packets += fixture.tick()

        fixture.target.playerInfo.avatar.hidden = true
        packets += fixture.tick()
        fixture.target.playerInfo.avatar.hidden = false
        packets += fixture.tick()

        val coveringWorld = fixture.allocWorld(1, ROOT_X ushr 3, activeLevel = 0, ROOT_X + 4)
        packets += fixture.tick()
        fixture.releaseWorld(coveringWorld)
        packets += fixture.tick()

        fixture.updateTarget(0, ROOT_X + 104, ROOT_Z)
        packets += fixture.tick()

        fixture.allocWorld(2, INSTANCE_ZONE_X, activeLevel = 0, ROOT_X + 4)
        fixture.updateObserver(instanceCoord(0, 0))
        fixture.updateTarget(instanceCoord(0, 1))
        packets += fixture.tick()
        fixture.updateTarget(instanceCoord(1, 1))
        packets += fixture.tick()
        return packets
    }

    @Test
    fun `final movement and player index reuse replace cached membership`() {
        val fixture = PlayerFixture()
        fixture.allocWorld(1, INSTANCE_ZONE_X, activeLevel = 0, ROOT_X + 104)

        fixture.updateObserver(0, ROOT_X, ROOT_Z)
        fixture.updateTarget(instanceCoord(0, 1))
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.updateTarget(instanceCoord(0, 1))
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        fixture.reallocateTarget(instanceCoord(0, 1))
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        fixture.reallocateTarget(CoordGrid(0, ROOT_X + 1, ROOT_Z))
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())
    }

    @Test
    fun `world entity projection movement uses current root coordinate`() {
        val fixture = PlayerFixture()
        val world = fixture.allocWorld(1, INSTANCE_ZONE_X, activeLevel = 0, ROOT_X + 104)

        fixture.updateObserver(instanceCoord(0, 0))
        fixture.updateTarget(0, ROOT_X + 1, ROOT_Z)
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())

        world.updateCoord(0, ROOT_X * 128 + 64, ROOT_Z * 128 + 64, teleport = true)
        fixture.tick()
        assertTrue(fixture.targetIsHighResolution())

        world.updateCoord(0, (ROOT_X + 104) * 128 + 64, ROOT_Z * 128 + 64, teleport = true)
        fixture.tick()
        assertFalse(fixture.targetIsHighResolution())
    }

    @Test
    fun `unlimited resize range preserves visibility bypass`() {
        val fixture = PlayerFixture()

        fixture.updateObserver(0, ROOT_X, ROOT_Z)
        fixture.updateTarget(3, ROOT_X + 104, ROOT_Z + 104)
        fixture.observer.playerInfo.avatar
            .forceResizeRange(Int.MAX_VALUE)
        fixture.tick()

        assertTrue(fixture.targetIsHighResolution())
    }

    private class PlayerFixture(
        playerProtocolWorker: ProtocolWorker? = null,
    ) {
        private val context =
            if (playerProtocolWorker == null) {
                generateInfoProtocolContext()
            } else {
                generateInfoProtocolContext(playerProtocolWorker = playerProtocolWorker)
            }
        private val protocols = context.protocols
        val observer: Infos = protocols.alloc(OBSERVER_INDEX, OldSchoolClientType.DESKTOP)
        var target: Infos
            private set

        init {
            updateObserver(0, ROOT_X, ROOT_Z)
            observer.updateRootBuildAreaCenteredOnPlayer(ROOT_X, ROOT_Z)
            val gpiBuffer = Unpooled.buffer(5000)
            observer.playerInfo.handleAbsolutePlayerPositions(gpiBuffer)
            gpiBuffer.release()
            target = protocols.alloc(TARGET_INDEX, OldSchoolClientType.DESKTOP)
            initializeAppearance(target.playerInfo)
        }

        fun updateObserver(
            level: Int,
            x: Int,
            z: Int,
        ) {
            observer.updateRootCoord(level, x, z)
        }

        fun updateObserver(coord: CoordGrid) = updateObserver(coord.level, coord.x, coord.z)

        fun updateTarget(
            level: Int,
            x: Int,
            z: Int,
        ) {
            target.updateRootCoord(level, x, z)
            target.updateRootBuildAreaCenteredOnPlayer(ROOT_X, ROOT_Z)
        }

        fun updateTarget(coord: CoordGrid) = updateTarget(coord.level, coord.x, coord.z)

        fun allocWorld(
            index: Int,
            zoneX: Int,
            activeLevel: Int,
            projectedX: Int,
            zoneZ: Int = if (zoneX == ROOT_X ushr 3) ROOT_Z ushr 3 else INSTANCE_ZONE_Z,
        ): WorldEntityAvatar =
            allocWorld(context.worldEntityAvatarFactory, index, zoneX, zoneZ, activeLevel, projectedX)

        fun releaseWorld(world: WorldEntityAvatar) {
            context.worldEntityAvatarFactory.release(world)
        }

        fun reallocateTarget(coord: CoordGrid = CoordGrid(0, ROOT_X + 1, ROOT_Z)) {
            protocols.dealloc(target)
            target = protocols.alloc(TARGET_INDEX, OldSchoolClientType.DESKTOP)
            updateTarget(coord)
            initializeAppearance(target.playerInfo)
        }

        fun tick(): String {
            protocols.worldEntityInfoProtocol.update()
            protocols.playerInfoProtocol.update()
            val observerBytes = packetHex(observer.playerInfo)
            packetHex(target.playerInfo)
            releaseWorldEntityPacket(observer)
            releaseWorldEntityPacket(target)
            return observerBytes
        }

        fun targetIsHighResolution(): Boolean = TARGET_INDEX in observer.playerInfo.getHighResolutionIndices()
    }

    private class StationarySnapshotFixture {
        private val context = generateInfoProtocolContext()
        private val protocols = context.protocols
        private val observer: Infos = protocols.alloc(OBSERVER_INDEX, OldSchoolClientType.DESKTOP)
        private val targets: Map<Int, Infos>

        init {
            observer.updateRootCoord(0, ROOT_X, ROOT_Z)
            observer.updateRootBuildAreaCenteredOnPlayer(ROOT_X, ROOT_Z)
            val gpiBuffer = Unpooled.buffer(5000)
            observer.playerInfo.handleAbsolutePlayerPositions(gpiBuffer)
            gpiBuffer.release()
            targets =
                listOf(10, 11, 20, 21).associateWith { index ->
                    protocols.alloc(index, OldSchoolClientType.DESKTOP).also { initializeAppearance(it.playerInfo) }
                }
        }

        fun updateTarget(
            index: Int,
            near: Boolean,
            xOffset: Int = 1,
        ) {
            val target = checkNotNull(targets[index])
            val x = if (near) ROOT_X + xOffset else ROOT_X + 104 + xOffset
            target.updateRootCoord(0, x, ROOT_Z)
            target.updateRootBuildAreaCenteredOnPlayer(ROOT_X, ROOT_Z)
        }

        fun tick(): String {
            protocols.worldEntityInfoProtocol.update()
            protocols.playerInfoProtocol.update()
            val observerBytes = packetHex(observer.playerInfo)
            for (target in targets.values) {
                packetHex(target.playerInfo)
            }
            releaseWorldEntityPacket(observer)
            for (target in targets.values) {
                releaseWorldEntityPacket(target)
            }
            return observerBytes
        }
    }

    private companion object {
        private const val OBSERVER_INDEX = 500
        private const val TARGET_INDEX = 10
        private const val ROOT_X = 3200
        private const val ROOT_Z = 3200
        private const val INSTANCE_ZONE_X = 800
        private const val INSTANCE_ZONE_Z = 800

        // Captured from the same scenario on unmodified upstream revision 239 at 7fa6050a.
        private val EXPECTED_PACKET_HEX =
            listOf(
                "00288640b202ff982041807f7f80808080808080808080808080808080808080808080808080808080807f7f" +
                    "7f7f7f7f7f7f7f7f7f7f7f7fd4e1f2e7e5f480fe808080808080808080",
                "807ff0",
                "007ff08640b200",
                "217ff0",
                "217ff0",
                "807ff0",
                "b8640190007ff08c80e400",
                "008a807ff0",
            )

        // Captured from the same scenario on unmodified upstream revision 239 at 7fa6050a.
        private val EXPECTED_STATIONARY_SNAPSHOT_PACKET_HEX =
            listOf(
                "00288640b2030c816405ff202041807f7f808080808080808080808080808080808080808080808080808080" +
                    "80807f7f7f7f7f7f7f7f7f7f7f7f7f7fd4e1f2e7e5f480fe8080808080808080802041807f7f808080808080" +
                    "80808080808080808080808080808080808080808080807f7f7f7f7f7f7f7f7f7f7f7f7f7fd4e1f2e7e5f480" +
                    "fe808080808080808080",
                "98427fec",
                "8080308640b2030c816405fe802041807f7f8080808080808080808080808080808080808080808080808080" +
                    "8080807f7f7f7f7f7f7f7f7f7f7f7f7f7fd4e1f2e7e5f480fe8080808080808080802041807f7f8080808080" +
                    "8080808080808080808080808080808080808080808080807f7f7f7f7f7f7f7f7f7f7f7f7f7fd4e1f2e7e5f4" +
                    "80fe808080808080808080",
            )

        private fun instanceCoord(
            level: Int,
            offset: Int,
            zoneX: Int = INSTANCE_ZONE_X,
        ): CoordGrid = CoordGrid(level, zoneX * 8 + offset, INSTANCE_ZONE_Z * 8)

        private fun allocWorld(
            factory: WorldEntityAvatarFactory,
            index: Int,
            zoneX: Int,
            zoneZ: Int,
            activeLevel: Int,
            projectedX: Int,
        ): WorldEntityAvatar =
            factory.alloc(
                index = index,
                id = index,
                ownerIndex = 0,
                sizeX = 1,
                sizeZ = 1,
                southWestZoneX = zoneX,
                southWestZoneZ = zoneZ,
                minLevel = 0,
                maxLevel = 1,
                fineX = projectedX * 128 + 64,
                fineZ = ROOT_Z * 128 + 64,
                projectedLevel = 0,
                activeLevel = activeLevel,
                angle = 0,
            )

        private fun initializeAppearance(player: PlayerInfo) {
            player.avatar.extendedInfo.setName("Target")
            player.avatar.extendedInfo.setCombatLevel(126)
            player.avatar.extendedInfo.setSkillLevel(0)
            player.avatar.extendedInfo.setHidden(false)
            player.avatar.extendedInfo.setBodyType(0)
            player.avatar.extendedInfo.setPronoun(0)
            player.avatar.extendedInfo.setSkullIcon(-1)
            player.avatar.extendedInfo.setOverheadIcon(-1)
        }

        private fun packetHex(player: PlayerInfo): String {
            val packet = checkNotNull(player.internalPacketResult().getOrNull())
            packet.consume()
            val buffer = packet.content()
            val bytes = ByteArray(buffer.readableBytes())
            buffer.getBytes(buffer.readerIndex(), bytes)
            packet.release()
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private fun releaseWorldEntityPacket(infos: Infos) {
            val packet =
                checkNotNull(
                    infos
                        .getPackets()
                        .rootWorldInfoPackets.worldEntityInfo
                        .getOrNull(),
                )
            packet.consume()
            packet.release()
        }
    }
}
