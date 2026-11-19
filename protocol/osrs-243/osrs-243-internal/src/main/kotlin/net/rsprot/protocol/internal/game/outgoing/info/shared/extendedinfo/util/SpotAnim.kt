package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * A value class to represent a spotanim in a primitive 'long'.
 * @param packed the bitpacked long value of this spotanim.
 */
@JvmInline
public value class SpotAnim(
    internal val packed: Long,
) {
    /**
     * @param id the id of the spotanim.
     * @param delay the delay in client cycles (20ms/cc) until the given spotanim begins rendering.
     * @param height the height at which to render the spotanim.
     * @param loop whether to loop the spotanim if the seq type allows it.
     */
    public constructor(
        id: Int,
        delay: Int,
        height: Int,
        loop: Boolean,
    ) : this(
        (id.toLong() and 0xFFFF)
            .or(delay.toLong() and 0xFFFF shl 16)
            .or(height.toLong() and 0xFFFF shl 32)
            .or(if (loop) (1L shl 48) else 0),
    )

    public val id: Int
        get() = (packed and 0xFFFF).toInt()
    public val delay: Int
        get() = (packed ushr 16 and 0xFFFF).toInt()
    public val height: Int
        get() = (packed ushr 32 and 0xFFFF).toInt()
    public val loop: Boolean
        get() = (packed ushr 48 and 0x1) == 0x1L

    public companion object {
        /**
         * The default value to initialize spotanim extended info as.
         */
        public val INVALID: SpotAnim = SpotAnim(-1L)
    }
}
