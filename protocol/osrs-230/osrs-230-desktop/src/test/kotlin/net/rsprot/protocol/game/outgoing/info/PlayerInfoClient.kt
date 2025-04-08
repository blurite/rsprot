package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

@Suppress("MemberVisibilityCanBePrivate", "CascadeIf")
class PlayerInfoClient {
    var localIndex: Int = -1
    var extendedInfoCount: Int = 0
    val extendedInfoIndices: IntArray = IntArray(2048)
    var highResolutionCount: Int = 0
    val highResolutionIndices: IntArray = IntArray(2048)
    var lowResolutionCount: Int = 0
    val lowResolutionIndices: IntArray = IntArray(2048)
    val unmodifiedFlags: ByteArray = ByteArray(2048)
    val cachedPlayers: Array<Player?> = arrayOfNulls(2048)
    val lowResolutionPositions: IntArray = IntArray(2048)

    fun gpiInit(
        localIndex: Int,
        bytebuf: ByteBuf,
    ) {
        this.localIndex = localIndex
        try {
            bytebuf.toBitBuf().use { buffer ->
                val localPlayer = Player(localIndex)
                cachedPlayers[localIndex] = localPlayer
                val coord = CoordGrid(buffer.gBits(30))
                localPlayer.coord = coord
                highResolutionCount = 0
                highResolutionIndices[highResolutionCount++] = localIndex
                unmodifiedFlags[localIndex] = 0
                lowResolutionCount = 0
                for (idx in 1..<2048) {
                    if (idx != localIndex) {
                        val lowResolutionPositionBitpacked = buffer.gBits(18)
                        val level = lowResolutionPositionBitpacked shr 16
                        // Note: In osrs, the 0xFF is actually 0x255, a mixture between hexadecimal and decimal numbering.
                        // This is likely just an oversight, but due to only the first bit being utilized,
                        // this never causes problems in OSRS
                        val x = lowResolutionPositionBitpacked shr 8 and 0xFF
                        val z = lowResolutionPositionBitpacked and 0xFF
                        lowResolutionPositions[idx] = (x shl 14) + z + (level shl 28)
                        lowResolutionIndices[lowResolutionCount++] = idx
                        unmodifiedFlags[idx] = 0
                    }
                }
            }
        } finally {
            bytebuf.release()
        }
    }

    fun decode(buffer: ByteBuf) {
        extendedInfoCount = 0
        try {
            decodeBitCodes(buffer)
        } finally {
            buffer.release()
        }
    }

    private fun decodeBitCodes(byteBuf: ByteBuf) {
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<highResolutionCount) {
                val idx = highResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE == 0) {
                    if (skipped > 0) {
                        --skipped
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else {
                            getHighResolutionPlayerPosition(buffer, idx)
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<highResolutionCount) {
                val idx = highResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE != 0) {
                    if (skipped > 0) {
                        --skipped
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else {
                            getHighResolutionPlayerPosition(buffer, idx)
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }

        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<lowResolutionCount) {
                val idx = lowResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE != 0) {
                    if (skipped > 0) {
                        --skipped
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else if (getLowResolutionPlayerPosition(buffer, idx)) {
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<lowResolutionCount) {
                val idx = lowResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE == 0) {
                    if (skipped > 0) {
                        --skipped
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else if (getLowResolutionPlayerPosition(buffer, idx)) {
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        lowResolutionCount = 0
        highResolutionCount = 0
        for (i in 1..<2048) {
            unmodifiedFlags[i] = (unmodifiedFlags[i].toInt() shr 1).toByte()
            val cachedPlayer = cachedPlayers[i]
            if (cachedPlayer != null) {
                highResolutionIndices[highResolutionCount++] = i
            } else {
                lowResolutionIndices[lowResolutionCount++] = i
            }
        }
        decodeExtendedInfo(byteBuf.toJagByteBuf())
    }

    private fun decodeExtendedInfo(buffer: JagByteBuf) {
        for (i in 0..<extendedInfoCount) {
            val index = extendedInfoIndices[i]
            val player = checkNotNull(cachedPlayers[index])
            var flag = buffer.g1()
            if (flag and 0x4 != 0) {
                flag += buffer.g1() shl 8
            }
            if (flag and 0x400 != 0) {
                flag += buffer.g1() shl 16
            }
            decodeExtendedInfoBlocks(buffer, player, flag)
        }
    }

    private fun decodeExtendedInfoBlocks(
        buffer: JagByteBuf,
        player: Player,
        flag: Int,
    ) {
        if (flag and 0x40 != 0) {
            val len = buffer.g1Alt2()
            val data = ByteArray(len)
            buffer.gdataAlt3(data)
            decodeAppearance(Unpooled.wrappedBuffer(data).toJagByteBuf(), player)
        }
        require(flag and 0x40.inv() == 0)
    }

    private fun decodeAppearance(
        buffer: JagByteBuf,
        player: Player,
    ) {
        player.gender = buffer.g1s()
        player.skullIcon = buffer.g1s()
        player.headIcon = buffer.g1s()
        val equipment = IntArray(12)
        player.equipment = equipment
        for (i in 0..<12) {
            val flag = buffer.g1()
            if (flag == 0) {
                equipment[i] = 0
                continue
            }
            val extra = buffer.g1()
            equipment[i] = (flag shl 8) + extra
            if (i == 0 && equipment[i] == 65535) {
                player.npcId = buffer.g2()
                break
            }
        }
        val identKit = IntArray(12)
        player.identKit = identKit
        for (i in 0..<12) {
            val value = buffer.g1()
            if (value == 0) {
                identKit[i] = 0
            } else {
                identKit[i] = (value shl 8) + buffer.g1()
            }
        }
        val colours = IntArray(5)
        player.colours = colours
        for (i in 0..<5) {
            colours[i] = buffer.g1()
        }
        player.readyAnim = buffer.g2()
        player.turnAnim = buffer.g2()
        player.walkAnim = buffer.g2()
        player.walkAnimBack = buffer.g2()
        player.walkAnimLeft = buffer.g2()
        player.walkAnimRight = buffer.g2()
        player.runAnim = buffer.g2()
        player.name = buffer.gjstr()
        player.combatLevel = buffer.g1()
        player.skillLevel = buffer.g2()
        player.hidden = buffer.g1() == 1
        val customisationFlag = buffer.g2()
        val hasCustomisations = customisationFlag shr 15 and 0x1 == 1
        if (hasCustomisations) {
            error("Not supported")
        }
        for (i in 0..<3) {
            player.nameExtras[i] = buffer.gjstr()
        }
        player.textGender = buffer.g1s()
    }

    private fun getHighResolutionPlayerPosition(
        buffer: BitBuf,
        idx: Int,
    ) {
        val extendedInfo = buffer.gBits(1) == 1
        if (extendedInfo) {
            extendedInfoIndices[extendedInfoCount++] = idx
        }
        val opcode = buffer.gBits(2)
        val cachedPlayer = checkNotNull(cachedPlayers[idx])
        if (opcode == 0) {
            if (extendedInfo) {
                cachedPlayer.queuedMove = false
            } else if (localIndex == idx) {
                throw RuntimeException()
            } else {
                lowResolutionPositions[idx] =
                    (cachedPlayer.coord.level shl 28)
                        .or(cachedPlayer.coord.z shr 13)
                        .or(cachedPlayer.coord.x shr 13 shl 14)
                cachedPlayers[idx] = null
                if (buffer.gBits(1) != 0) {
                    getLowResolutionPlayerPosition(buffer, idx)
                }
            }
        } else if (opcode == 1) {
            val movementOpcode = buffer.gBits(3)
            var curX = cachedPlayer.coord.x
            var curZ = cachedPlayer.coord.z
            if (movementOpcode == 0) {
                --curX
                --curZ
            } else if (movementOpcode == 1) {
                --curZ
            } else if (movementOpcode == 2) {
                ++curX
                --curZ
            } else if (movementOpcode == 3) {
                --curX
            } else if (movementOpcode == 4) {
                ++curX
            } else if (movementOpcode == 5) {
                --curX
                ++curZ
            } else if (movementOpcode == 6) {
                ++curZ
            } else if (movementOpcode == 7) {
                ++curX
                ++curZ
            }
            cachedPlayer.coord = CoordGrid(cachedPlayer.coord.level, curX, curZ)
            cachedPlayer.queuedMove = extendedInfo
        } else if (opcode == 2) {
            val movementOpcode = buffer.gBits(4)
            var curX = cachedPlayer.coord.x
            var curZ = cachedPlayer.coord.z
            if (movementOpcode == 0) {
                curX -= 2
                curZ -= 2
            } else if (movementOpcode == 1) {
                --curX
                curZ -= 2
            } else if (movementOpcode == 2) {
                curZ -= 2
            } else if (movementOpcode == 3) {
                ++curX
                curZ -= 2
            } else if (movementOpcode == 4) {
                curX += 2
                curZ -= 2
            } else if (movementOpcode == 5) {
                curX -= 2
                --curZ
            } else if (movementOpcode == 6) {
                curX += 2
                --curZ
            } else if (movementOpcode == 7) {
                curX -= 2
            } else if (movementOpcode == 8) {
                curX += 2
            } else if (movementOpcode == 9) {
                curX -= 2
                ++curZ
            } else if (movementOpcode == 10) {
                curX += 2
                ++curZ
            } else if (movementOpcode == 11) {
                curX -= 2
                curZ += 2
            } else if (movementOpcode == 12) {
                --curX
                curZ += 2
            } else if (movementOpcode == 13) {
                curZ += 2
            } else if (movementOpcode == 14) {
                ++curX
                curZ += 2
            } else if (movementOpcode == 15) {
                curX += 2
                curZ += 2
            }
            cachedPlayer.coord = CoordGrid(cachedPlayer.coord.level, curX, curZ)
            cachedPlayer.queuedMove = extendedInfo
        } else {
            val far = buffer.gBits(1)
            if (far == 0) {
                val coord = buffer.gBits(12)
                val deltaLevel = coord shr 10
                var deltaX = coord shr 5 and 31
                if (deltaX > 15) {
                    deltaX -= 32
                }
                var deltaZ = coord and 31
                if (deltaZ > 15) {
                    deltaZ -= 32
                }
                var curLevel = cachedPlayer.coord.level
                var curX = cachedPlayer.coord.x
                var curZ = cachedPlayer.coord.z
                curX += deltaX
                curZ += deltaZ
                curLevel = (curLevel + deltaLevel) and 0x3
                cachedPlayer.coord = CoordGrid(curLevel, curX, curZ)
                cachedPlayer.queuedMove = extendedInfo
            } else {
                val coord = buffer.gBits(30)
                val deltaLevel = coord shr 28
                val deltaX = coord shr 14 and 16383
                val deltaZ = coord and 16383
                var curLevel = cachedPlayer.coord.level
                var curX = cachedPlayer.coord.x
                var curZ = cachedPlayer.coord.z
                curX = (curX + deltaX) and 16383
                curZ = (curZ + deltaZ) and 16383
                curLevel = (curLevel + deltaLevel) and 0x3
                cachedPlayer.coord = CoordGrid(curLevel, curX, curZ)
                cachedPlayer.queuedMove = extendedInfo
            }
        }
    }

    private fun getLowResolutionPlayerPosition(
        buffer: BitBuf,
        idx: Int,
    ): Boolean {
        val opcode = buffer.gBits(2)
        if (opcode == 0) {
            if (buffer.gBits(1) != 0) {
                getLowResolutionPlayerPosition(buffer, idx)
            }
            val x = buffer.gBits(13)
            val z = buffer.gBits(13)
            val extendedInfo = buffer.gBits(1) == 1
            if (extendedInfo) {
                this.extendedInfoIndices[extendedInfoCount++] = idx
            }
            if (cachedPlayers[idx] != null) {
                throw RuntimeException()
            }
            val player = Player(idx)
            cachedPlayers[idx] = player
            // cached appearance decoding
            val lowResolutionPosition = lowResolutionPositions[idx]
            val level = lowResolutionPosition shr 28
            val lowResX = lowResolutionPosition shr 14 and 0xFF
            val lowResZ = lowResolutionPosition and 0xFF
            player.coord = CoordGrid(level, (lowResX shl 13) + x, (lowResZ shl 13) + z)
            player.queuedMove = false
            return true
        } else if (opcode == 1) {
            val levelDelta = buffer.gBits(2)
            val lowResPosition = lowResolutionPositions[idx]
            lowResolutionPositions[idx] =
                ((lowResPosition shr 28) + levelDelta and 3 shl 28)
                    .plus(lowResPosition and 268435455)
            return false
        } else if (opcode == 2) {
            val bitpacked = buffer.gBits(5)
            val levelDelta = bitpacked shr 3
            val movementCode = bitpacked and 7
            val lowResPosition = lowResolutionPositions[idx]
            val level = (lowResPosition shr 28) + levelDelta and 3
            var x = lowResPosition shr 14 and 255
            var z = lowResPosition and 255
            if (movementCode == 0) {
                --x
                --z
            }

            if (movementCode == 1) {
                --z
            }

            if (movementCode == 2) {
                ++x
                --z
            }

            if (movementCode == 3) {
                --x
            }

            if (movementCode == 4) {
                ++x
            }

            if (movementCode == 5) {
                --x
                ++z
            }

            if (movementCode == 6) {
                ++z
            }

            if (movementCode == 7) {
                ++x
                ++z
            }
            lowResolutionPositions[idx] = (x shl 14) + z + (level shl 28)
            return false
        } else {
            val bitpacked = buffer.gBits(18)
            val levelDelta = bitpacked shr 16
            val xDelta = bitpacked shr 8 and 255
            val zDelta = bitpacked and 255
            val lowResPosition = lowResolutionPositions[idx]
            val level = (lowResPosition shr 28) + levelDelta and 3
            val x = xDelta + (lowResPosition shr 14) and 255
            val z = zDelta + lowResPosition and 255
            lowResolutionPositions[idx] = (x shl 14) + z + (level shl 28)
            return false
        }
    }

    private fun readStationary(buffer: BitBuf): Int {
        val type = buffer.gBits(2)
        return when (type) {
            0 -> 0
            1 -> buffer.gBits(5)
            2 -> buffer.gBits(8)
            else -> buffer.gBits(11)
        }
    }

    companion object {
        private const val CUR_CYCLE_INACTIVE = 0x1
        private const val NEXT_CYCLE_INACTIVE = 0x2

        class Player(
            val playerId: Int,
        ) {
            var queuedMove: Boolean = false
            var coord: CoordGrid = CoordGrid.INVALID
            var skullIcon: Int = -1
            var headIcon: Int = -1
            var npcId: Int = -1
            var readyAnim: Int = -1
            var turnAnim: Int = -1
            var walkAnim: Int = -1
            var walkAnimBack: Int = -1
            var walkAnimLeft: Int = -1
            var walkAnimRight: Int = -1
            var runAnim: Int = -1
            var name: String? = null
            var combatLevel: Int = 0
            var skillLevel: Int = 0
            var hidden: Boolean = false
            var nameExtras: Array<String> = Array(3) { "" }
            var textGender: Int = 0
            var gender: Int = 0
            var equipment: IntArray = IntArray(12)
            var identKit: IntArray = IntArray(12)
            var colours: IntArray = IntArray(5)

            override fun toString(): String =
                "Player(" +
                    "playerId=$playerId, " +
                    "queuedMove=$queuedMove, " +
                    "coord=$coord, " +
                    "skullIcon=$skullIcon, " +
                    "headIcon=$headIcon, " +
                    "npcId=$npcId, " +
                    "readyAnim=$readyAnim, " +
                    "turnAnim=$turnAnim, " +
                    "walkAnim=$walkAnim, " +
                    "walkAnimBack=$walkAnimBack, " +
                    "walkAnimLeft=$walkAnimLeft, " +
                    "walkAnimRight=$walkAnimRight, " +
                    "runAnim=$runAnim, " +
                    "name=$name, " +
                    "combatLevel=$combatLevel, " +
                    "skillLevel=$skillLevel, " +
                    "hidden=$hidden, " +
                    "nameExtras=${nameExtras.contentToString()}, " +
                    "textGender=$textGender, " +
                    "gender=$gender, " +
                    "equipment=${equipment.contentToString()}, " +
                    "identKit=${identKit.contentToString()}, " +
                    "colours=${colours.contentToString()}" +
                    ")"
        }
    }
}
