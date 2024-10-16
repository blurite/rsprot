package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.outgoing.info.util.BuildArea

/**
 * Checks if the [avatar] is inside the specified build area.
 * @return whether this build area fully contains the [avatar].
 */
internal operator fun BuildArea.contains(avatar: WorldEntityAvatar): Boolean {
    val minBuildAreaZoneX = this.zoneX
    val minBuildAreaZoneZ = this.zoneZ
    val coord = avatar.currentCoordFine
    val minAvatarZoneX = coord.x ushr 3
    val minAvatarZoneZ = coord.z ushr 3
    if (minAvatarZoneX < minBuildAreaZoneX || minAvatarZoneZ < minBuildAreaZoneZ) {
        return false
    }
    val maxBuildAreaZoneX = minBuildAreaZoneX + this.widthInZones
    val maxBuildAreaZoneZ = minBuildAreaZoneZ + this.heightInZones
    val maxAvatarZoneX = minAvatarZoneX + avatar.sizeX
    val maxAvatarZoneZ = minAvatarZoneZ + avatar.sizeZ
    return !(maxAvatarZoneX > maxBuildAreaZoneX || maxAvatarZoneZ > maxBuildAreaZoneZ)
}

internal fun JagByteBuf.encodeAngledCoordFine(
    x: Int,
    y: Int,
    z: Int,
    angle: Int,
) {
    val marker = writerIndex()
    p1(0)
    var opcode = 0
    if (x != 0) {
        opcode = opcode or runLengthEncode(x)
    }
    if (y != 0) {
        opcode = opcode or (runLengthEncode(y) shl 2)
    }
    if (z != 0) {
        opcode = opcode or (runLengthEncode(z) shl 4)
    }
    if (angle != 0) {
        opcode = opcode or (runLengthEncode(angle) shl 6)
    }
    val index = writerIndex()
    writerIndex(marker)
    p1(opcode)
    writerIndex(index)
}

private fun JagByteBuf.runLengthEncode(value: Int): Int {
    when (value) {
        0 -> {
            return 0
        }
        in Byte.MIN_VALUE..Byte.MAX_VALUE -> {
            p1(value)
            return 1
        }
        in Short.MIN_VALUE..Short.MAX_VALUE -> {
            p2(value)
            return 2
        }
        else -> {
            p4(value)
            return 3
        }
    }
}
