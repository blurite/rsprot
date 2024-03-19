package net.rsprot.protocol.loginprot.incoming.util

/**
 * A value class to hold the password.
 * This class offers additional functionality to clear the data from memory,
 * to avoid any potential memory attacks.
 */
@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
public value class Password(
    public val data: ByteArray,
) {
    /**
     * Returns the string representation of the password.
     */
    public fun asString(): String {
        return String(data)
    }

    /**
     * Clears the data, setting all bytes to 0.
     * Once [clear] has been called, the password will no longer be in memory.
     */
    public fun clear() {
        data.fill(0)
    }
}
