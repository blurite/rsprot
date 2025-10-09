package net.rsprot.protocol.loginprot.incoming.util

/**
 * A class to hold the login token.
 * This class offers additional functionality to clear the data from memory,
 * to avoid any potential memory attacks.
 */
public class Token(
    public val data: ByteArray,
) {
    /**
     * Returns the string representation of the token.
     */
    public fun asString(): String = String(data)

    /**
     * Clears the data, setting all bytes to 0.
     * Once [clear] has been called, the token will no longer be in memory.
     */
    public fun clear() {
        data.fill(0)
    }
}
