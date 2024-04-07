package net.rsprot.protocol.game.outgoing.zone.payload.util

/**
 * Coord in build-area is a helper class to compress the data used to transmit
 * build-area coords to the client, primarily in *-specific packets.
 * These packets will separate the south-western zone X/Z coordinates,
 * and the x/z in-zone coordinates into separate properties.
 * @property zoneX the south-western x coordinate of the zone (multiples of 8 value)
 * @property xInZone the x coordinate within the zone (0-7 value)
 * @property zoneZ the south-western z coordinate of the zone (multiples of 8 value)
 * @property zInZone the z coordinate within the zone (0-7 value)
 * @property bitpackedMedium the coordinates bitpacked into a 24-bit integer,
 * as this is how they tend to be transmitted to the client.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
@JvmInline
internal value class CoordInBuildArea private constructor(
    private val packed: UShort,
) {
    constructor(
        zoneX: Int,
        xInZone: Int,
        zoneZ: Int,
        zInZone: Int,
    ) : this(
        ((zoneX and 0x7.inv() or (xInZone and 0x7)) shl 8)
            .or((zoneZ and 0x7.inv()) or (zInZone and 0x7))
            .toUShort(),
    )

    val zoneX: Int
        get() = packed.toInt() ushr 8 and 0xF8
    val xInZone: Int
        get() = packed.toInt() ushr 8 and 0x7
    val zoneZ: Int
        get() = packed.toInt() and 0xF8
    val zInZone: Int
        get() = packed.toInt() and 0x7

    val bitpackedMedium: Int
        get() =
            (zoneX shl 16)
                .or(zoneZ shl 8)
                .or(xInZone shl 4)
                .or(zInZone)
}
