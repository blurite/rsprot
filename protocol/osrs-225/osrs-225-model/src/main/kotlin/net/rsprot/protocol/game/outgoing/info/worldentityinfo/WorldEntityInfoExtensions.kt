package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.game.outgoing.info.util.BuildArea

/**
 * Checks if the [avatar] is inside the specified build area.
 * @return whether this build area fully contains the [avatar].
 */
internal operator fun BuildArea.contains(avatar: WorldEntityAvatar): Boolean {
    val minBuildAreaZoneX = this.zoneX
    val minBuildAreaZoneZ = this.zoneZ
    val coord = avatar.currentCoord
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
