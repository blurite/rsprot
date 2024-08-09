package net.rsprot.protocol.util

@JvmInline
public value class CombinedId(
    public val combinedId: Int,
) {
    public constructor(
        interfaceId: Int,
        componentId: Int,
    ) : this(
        (interfaceId and 0xFFFF shl 16)
            .or(componentId and 0xFFFF),
    )

    public val interfaceId: Int
        get() {
            val value = combinedId ushr 16 and 0xFFFF
            return if (value == 0xFFFF) {
                -1
            } else {
                value
            }
        }

    public val componentId: Int
        get() {
            val value = combinedId and 0xFFFF
            return if (value == 0xFFFF) {
                -1
            } else {
                value
            }
        }

    public operator fun component1(): Int = interfaceId

    public operator fun component2(): Int = componentId

    override fun toString(): String =
        "CombinedId(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
}
