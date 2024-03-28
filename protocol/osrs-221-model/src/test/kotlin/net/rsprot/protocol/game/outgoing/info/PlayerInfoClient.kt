package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
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
        BitBuf(bytebuf).use { buffer ->
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
    }

    fun decode(buffer: ByteBuf) {
        extendedInfoCount = 0
        decodeBitCodes(buffer)
    }

    private fun decodeBitCodes(byteBuf: ByteBuf) {
        BitBuf(byteBuf).use { buffer ->
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
        BitBuf(byteBuf).use { buffer ->
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

        BitBuf(byteBuf).use { buffer ->
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
        BitBuf(byteBuf).use { buffer ->
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
        // TODO: Extended info blocks
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
            val var3 = buffer.gBits(2)
            val var4 = lowResolutionPositions[idx]
            lowResolutionPositions[idx] = ((var4 shr 28) + var3 and 3 shl 28) + (var4 and 268435455)
            return false
        } else if (opcode == 2) {
            val var3 = buffer.gBits(5)
            val var4 = var3 shr 3
            val var5 = var3 and 7
            val var11 = lowResolutionPositions[idx]
            val var7 = (var11 shr 28) + var4 and 3
            var var8 = var11 shr 14 and 255
            var var9 = var11 and 255
            if (var5 == 0) {
                --var8
                --var9
            }

            if (var5 == 1) {
                --var9
            }

            if (var5 == 2) {
                ++var8
                --var9
            }

            if (var5 == 3) {
                --var8
            }

            if (var5 == 4) {
                ++var8
            }

            if (var5 == 5) {
                --var8
                ++var9
            }

            if (var5 == 6) {
                ++var9
            }

            if (var5 == 7) {
                ++var8
                ++var9
            }
            lowResolutionPositions[idx] = (var8 shl 14) + var9 + (var7 shl 28)
            return false
        } else {
            val var3 = buffer.gBits(18)
            val var4 = var3 shr 16
            val var5 = var3 shr 8 and 255
            val var11 = var3 and 255
            val var7 = lowResolutionPositions[idx]
            val var8 = (var7 shr 28) + var4 and 3
            val var9 = var5 + (var7 shr 14) and 255
            val var10 = var11 + var7 and 255
            lowResolutionPositions[idx] = (var9 shl 14) + var10 + (var8 shl 28)
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

        class Player(val playerId: Int) {
            var queuedMove: Boolean = false
            var coord: CoordGrid = CoordGrid.INVALID

            override fun toString(): String {
                return "Player(playerId=$playerId, queuedMove=$queuedMove, coord=$coord)"
            }
        }
    }
}
