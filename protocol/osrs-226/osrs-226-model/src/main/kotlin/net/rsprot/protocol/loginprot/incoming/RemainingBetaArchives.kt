package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingLoginMessage

public class RemainingBetaArchives(
    payload: ByteArray,
) : IncomingLoginMessage {
    private val payload: ByteArray = payload.copyOf()

    init {
        require(payload.size == PAYLOAD_SIZE) {
            "Expected $PAYLOAD_SIZE payload bytes, got ${payload.size}"
        }
    }

    public fun toByteArray(): ByteArray = payload.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemainingBetaArchives

        return payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int = payload.contentHashCode()

    override fun toString(): String = "RemainingBetaArchives(payloadSize=${payload.size})"

    public companion object {
        public const val PAYLOAD_SIZE: Int = 56

        public val protectedArchives: List<Int> =
            listOf(
                0,
                1,
                2,
                3,
                5,
                7,
                9,
                11,
                12,
                16,
                17,
                18,
                19,
                20,
            )
    }
}
