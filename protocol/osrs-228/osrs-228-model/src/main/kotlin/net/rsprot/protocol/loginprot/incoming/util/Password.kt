package net.rsprot.protocol.loginprot.incoming.util

import java.nio.ByteBuffer

/**
 * A class to hold the password.
 * This class offers additional functionality to clear the data from memory,
 * to avoid any potential memory attacks.
 */
public class Password(
    public val data: ByteArray,
) {
    /**
     * Returns the string representation of the password.
     *
     * WARNING: As this constructs a string, it will be internalized and stored in memory.
     * This means anyone capturing a heap dump is very likely to also capture some lingering
     * passwords still in memory.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun asString(): String = String(data)

    /**
     * Returns the char array representation of the password.
     * This function does not create intermediate strings which would linger indefinitely
     * in memory. The intermediate char buffer is also cleared after use.
     * The [data] will not be automatically cleared. In order to do so, invoke [clear].
     */
    public fun asCharArray(): CharArray {
        val byteBuffer = ByteBuffer.wrap(data)
        val charBuffer = Charsets.UTF_8.decode(byteBuffer)
        return try {
            CharArray(charBuffer.remaining()).apply { charBuffer.get(this) }
        } finally {
            // Rewind the buffer to the very start after reading
            charBuffer.rewind()

            // Manually overwrite all the values with the 0-byte character
            val position = charBuffer.position()
            val limit = charBuffer.limit()
            for (i in position..<limit) {
                charBuffer.put(i, 0.toChar())
            }

            // Furthermore, erase the pointers of this buffer.
            charBuffer.clear()
        }
    }

    /**
     * Clears the data, setting all bytes to 0.
     * Once [clear] has been called, the password will no longer be in memory.
     */
    public fun clear() {
        data.fill(0)
    }
}
