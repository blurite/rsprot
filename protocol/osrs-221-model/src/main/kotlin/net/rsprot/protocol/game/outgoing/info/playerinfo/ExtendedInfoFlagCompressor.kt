package net.rsprot.protocol.game.outgoing.info.playerinfo

/**
 * A class to compress observable extended info flags to fit into a single byte.
 * In doing so, we can switch from storing observer flags in int[2048] down to byte[2048],
 * which provides a noticeable performance improvement as a result.
 * @param appearance the original appearance flag that the client expects
 * @param moveSpeed the original move speed flag that the client expects
 * @param facePathingEntity the original face pathingentity flag that the client expects
 * @param tinting the original tinting flag that the client expects
 */
public class ExtendedInfoFlagCompressor(
    appearance: Int,
    moveSpeed: Int,
    facePathingEntity: Int,
    tinting: Int,
) {
    /**
     * The bit-shift necessary to transform
     * the [COMPRESSED_APPEARANCE] flag back into the decompressed variant.
     */
    private val appearanceShift: Int = offset(appearance, COMPRESSED_APPEARANCE)

    /**
     * The bit-shift necessary to transform
     * the [COMPRESSED_MOVE_SPEED] flag back into the decompressed variant.
     */
    private val moveSpeedShift: Int = offset(moveSpeed, COMPRESSED_MOVE_SPEED)

    /**
     * The bit-shift necessary to transform
     * the [COMPRESSED_FACE_PATHINGENTITY] flag back into the decompressed variant.
     */
    private val facePathingEntityShift: Int = offset(facePathingEntity, COMPRESSED_FACE_PATHINGENTITY)

    /**
     * The bit-shift necessary to transform
     * the [COMPRESSED_TINTING] flag back into the decompressed variant.
     */
    private val tintingShift: Int = offset(tinting, COMPRESSED_TINTING)

    /**
     * Calculates the bit-shift offset necessary to transform the [compressed]
     * flag back to the [decompressed] variant.
     * @param decompressed the original decompressed flag that the client expects
     * @param compressed the compressed version of that flag, made to fit into a byte.
     * @return the number of bits to shift the compressed variant by to return the
     * original decompressed flag.
     */
    private fun offset(
        decompressed: Int,
        compressed: Int,
    ): Int {
        return decompressed.countTrailingZeroBits() - compressed.countTrailingZeroBits()
    }

    /**
     * Decompresses the compressed [value] flag back to the decompressed
     * variant that the client expects.
     * @param value the compressed bit-flag to decompress.
     * @return the decompressed bit-flag.
     */
    public fun decompress(value: Int): Int {
        return (value and COMPRESSED_APPEARANCE shl appearanceShift)
            .or(value and COMPRESSED_MOVE_SPEED shl moveSpeedShift)
            .or(value and COMPRESSED_FACE_PATHINGENTITY shl facePathingEntityShift)
            .or(value and COMPRESSED_TINTING shl tintingShift)
    }

    public companion object {
        /**
         * The compressed variant of appearance flag, made to fit into a byte.
         */
        public const val COMPRESSED_APPEARANCE: Int = 0x1

        /**
         * The compressed variant of move speed flag, made to fit into a byte.
         */
        public const val COMPRESSED_MOVE_SPEED: Int = 0x2

        /**
         * The compressed variant of face pathingentity flag, made to fit into a byte.
         */
        public const val COMPRESSED_FACE_PATHINGENTITY: Int = 0x4

        /**
         * The compressed variant of tinting flag, made to fit into a byte.
         */
        public const val COMPRESSED_TINTING: Int = 0x8
    }
}
