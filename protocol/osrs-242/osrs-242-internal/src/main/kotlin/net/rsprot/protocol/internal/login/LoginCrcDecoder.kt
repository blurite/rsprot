package net.rsprot.protocol.internal.login

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType

/**
 * Decodes the platform-specific CRC structures transmitted during login.
 */
public interface LoginCrcDecoder {
    public val clientType: OldSchoolClientType

    public fun decodeLive(buffer: JagByteBuf): IntArray

    public fun decodeInitialBeta(buffer: JagByteBuf): IntArray

    public fun decodeRemainingBeta(buffer: JagByteBuf): IntArray

    public companion object {
        public const val CRC_COUNT: Int = 23
    }
}
