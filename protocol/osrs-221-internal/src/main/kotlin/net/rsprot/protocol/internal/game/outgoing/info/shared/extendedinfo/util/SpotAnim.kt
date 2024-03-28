package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

@JvmInline
public value class SpotAnim(internal val packed: Long) {
    public constructor(
        id: Int,
        delay: Int,
        height: Int,
    ) : this(
        (id.toLong() and 0xFFFF)
            .or(delay.toLong() and 0xFFFF shl 16)
            .or(height.toLong() and 0xFFFF shl 32),
    )

    public val id: Int
        get() = (packed and 0xFFFF).toInt()
    public val delay: Int
        get() = (packed ushr 16 and 0xFFFF).toInt()
    public val height: Int
        get() = (packed ushr 32 and 0xFFFF).toInt()

    public companion object {
        public val INVALID: SpotAnim = SpotAnim(-1L)
    }
}
