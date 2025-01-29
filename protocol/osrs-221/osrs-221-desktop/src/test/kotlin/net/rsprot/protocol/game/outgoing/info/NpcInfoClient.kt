package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

@Suppress("MemberVisibilityCanBePrivate")
class NpcInfoClient {
    var deletedNpcCount: Int = 0
    var deletedNpcSlot = IntArray(1000)
    var cachedNpcs = arrayOfNulls<Npc>(65536)
    var npcSlotCount = 0
    var npcSlot = IntArray(65536)
    var updatedNpcSlotCount: Int = 0
    var updatedNpcSlot: IntArray = IntArray(250)

    var cycle = 0

    fun decode(
        buffer: ByteBuf,
        large: Boolean,
        localPlayerCoord: CoordGrid,
    ) {
        deletedNpcCount = 0
        updatedNpcSlotCount = 0
        try {
            buffer.toBitBuf().use { bitBuffer ->
                processHighResolution(bitBuffer)
                processLowResolution(large, bitBuffer, localPlayerCoord)
            }
            processExtendedInfo(buffer.toJagByteBuf())
            for (i in 0..<deletedNpcCount) {
                val index = deletedNpcSlot[i]
                if (cycle != checkNotNull(cachedNpcs[index]).lastUpdateCycle) {
                    cachedNpcs[index] = null
                }
            }
            if (buffer.isReadable) {
                throw IllegalStateException("npc info buffer still readable: ${buffer.readableBytes()}")
            }
        } finally {
            buffer.release()
        }
        for (i in 0..<npcSlotCount) {
            if (cachedNpcs[npcSlot[i]] == null) {
                throw IllegalStateException("Npc null at i $i")
            }
        }
        cycle++
    }

    private fun processExtendedInfo(buffer: JagByteBuf) {
        for (i in 0..<updatedNpcSlotCount) {
            val index = updatedNpcSlot[i]
            val npc = checkNotNull(cachedNpcs[index])
            var flag = buffer.g1()
            if ((flag and 0x2) != 0) {
                val extra: Int = buffer.g1()
                flag += extra shl 8
            }
            if ((flag and 0x100) != 0) {
                val extra: Int = buffer.g1()
                flag += extra shl 16
            }
            check(flag and (0x2 or 0x100 or 0x1).inv() == 0) {
                "Extended info other than 'say' included!"
            }
            if (flag and 0x1 != 0) {
                val text = buffer.gjstr()
                npc.overheadChat = text
            }
        }
    }

    private fun processHighResolution(buffer: BitBuf) {
        val count = buffer.gBits(8)
        if (count < npcSlotCount) {
            for (i in count..<npcSlotCount) {
                deletedNpcSlot[deletedNpcCount++] = npcSlot[i]
            }
        }
        require(count <= npcSlotCount) {
            "Too many npcs to process: $count, $npcSlotCount"
        }
        npcSlotCount = 0
        for (i in 0..<count) {
            val index = npcSlot[i]
            val npc = requireNotNull(cachedNpcs[i])
            val hasUpdate = buffer.gBits(1)
            if (hasUpdate == 0) {
                npcSlot[npcSlotCount++] = index
                npc.lastUpdateCycle = cycle
                continue
            }
            val updateType = buffer.gBits(2)
            if (updateType == 0) {
                npcSlot[npcSlotCount++] = index
                npc.lastUpdateCycle = cycle
                updatedNpcSlot[updatedNpcSlotCount++] = index
            } else if (updateType == 1) {
                npcSlot[npcSlotCount++] = index
                npc.lastUpdateCycle = cycle
                val walkDirection = buffer.gBits(3)
                npc.addRouteWaypointAdjacent(walkDirection, MoveSpeed.WALK)
                val extendedInfo = buffer.gBits(1)
                if (extendedInfo == 1) {
                    updatedNpcSlot[updatedNpcSlotCount++] = index
                }
            } else if (updateType == 2) {
                npcSlot[npcSlotCount++] = index
                npc.lastUpdateCycle = cycle
                if (buffer.gBits(1) == 1) {
                    val walkDirection = buffer.gBits(3)
                    npc.addRouteWaypointAdjacent(walkDirection, MoveSpeed.RUN)
                    val runDirection = buffer.gBits(3)
                    npc.addRouteWaypointAdjacent(runDirection, MoveSpeed.RUN)
                } else {
                    val crawlDirection = buffer.gBits(3)
                    npc.addRouteWaypointAdjacent(crawlDirection, MoveSpeed.CRAWL)
                }
                val extendedInfo = buffer.gBits(1)
                if (extendedInfo == 1) {
                    updatedNpcSlot[updatedNpcSlotCount++] = index
                }
            } else if (updateType == 3) {
                deletedNpcSlot[deletedNpcCount++] = index
            }
        }
    }

    private fun processLowResolution(
        large: Boolean,
        buffer: BitBuf,
        localPlayerCoord: CoordGrid,
    ) {
        while (true) {
            val indexBitCount = 16
            val capacity = (1 shl indexBitCount)
            if (buffer.readableBits() >= indexBitCount + 12) {
                val index = buffer.gBits(indexBitCount)
                if (capacity - 1 != index) {
                    var isNew = false
                    if (cachedNpcs[index] == null) {
                        cachedNpcs[index] =
                            Npc(index, -1, CoordGrid.INVALID)
                        isNew = true
                    }
                    val npc = checkNotNull(cachedNpcs[index])
                    npcSlot[npcSlotCount++] = index
                    npc.lastUpdateCycle = cycle
                    val deltaZ = decodeDelta(large, buffer)
                    val extendedInfo = buffer.gBits(1)
                    if (extendedInfo == 1) {
                        updatedNpcSlot[updatedNpcSlotCount++] = index
                    }
                    val jump = buffer.gBits(1)
                    val angle = NPC_TURN_ANGLES[buffer.gBits(3)]
                    if (isNew) {
                        npc.turnAngle = angle
                        npc.angle = angle
                    }
                    npc.id = buffer.gBits(14)
                    val deltaX = decodeDelta(large, buffer)
                    val hasSpawnCycle = buffer.gBits(1) == 1
                    if (hasSpawnCycle) {
                        npc.spawnCycle = buffer.gBits(32)
                    }
                    // reset bas
                    if (npc.turnSpeed == 0) {
                        npc.angle = 0
                    }
                    npc.addRouteWaypoint(
                        localPlayerCoord,
                        deltaX,
                        deltaZ,
                        jump == 1,
                    )
                    continue
                }
            }
            return
        }
    }

    private fun decodeDelta(
        large: Boolean,
        buffer: BitBuf,
    ): Int =
        if (large) {
            var delta = buffer.gBits(8)
            if (delta > 127) {
                delta -= 256
            }
            delta
        } else {
            var delta = buffer.gBits(5)
            if (delta > 15) {
                delta -= 32
            }
            delta
        }

    class Npc(
        val index: Int,
        var id: Int,
        var coord: CoordGrid,
    ) {
        var lastUpdateCycle: Int = 0
        var moveSpeed: MoveSpeed = MoveSpeed.STATIONARY
        var turnAngle = 0
        var angle = 0
        var spawnCycle = 0
        var turnSpeed = 32
        var jump: Boolean = false
        var overheadChat: String? = null

        fun addRouteWaypoint(
            localPlayerCoord: CoordGrid,
            relativeX: Int,
            relativeZ: Int,
            jump: Boolean,
        ) {
            coord =
                CoordGrid(
                    localPlayerCoord.level,
                    localPlayerCoord.x + relativeX,
                    localPlayerCoord.z + relativeZ,
                )
            moveSpeed = MoveSpeed.STATIONARY
            this.jump = jump
        }

        fun addRouteWaypointAdjacent(
            opcode: Int,
            speed: MoveSpeed,
        ) {
            var x: Int = coord.x
            var z: Int = coord.z
            if (opcode == 0) {
                --x
                ++z
            }

            if (opcode == 1) {
                ++z
            }

            if (opcode == 2) {
                ++x
                ++z
            }

            if (opcode == 3) {
                --x
            }

            if (opcode == 4) {
                ++x
            }

            if (opcode == 5) {
                --x
                --z
            }

            if (opcode == 6) {
                --z
            }

            if (opcode == 7) {
                ++x
                --z
            }

            coord =
                CoordGrid(coord.level, x, z)
            moveSpeed = speed
        }

        override fun toString(): String =
            "Npc(" +
                "index=$index, " +
                "id=$id, " +
                "coord=$coord, " +
                "lastUpdateCycle=$lastUpdateCycle, " +
                "moveSpeed=$moveSpeed, " +
                "turnAngle=$turnAngle, " +
                "angle=$angle, " +
                "spawnCycle=$spawnCycle, " +
                "turnSpeed=$turnSpeed, " +
                "jump=$jump" +
                ")"
    }

    enum class MoveSpeed(
        @Suppress("unused") val id: Int,
    ) {
        STATIONARY(-1),
        CRAWL(0),
        WALK(1),
        RUN(2),
    }

    private companion object {
        private val NPC_TURN_ANGLES = intArrayOf(768, 1024, 1280, 512, 1536, 256, 0, 1792)
    }
}
