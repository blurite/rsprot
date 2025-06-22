package net.rsprot.protocol.loginprot.incoming.util

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
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun asString(): String = String(data)

    /**
     * Returns the char array representation of the password.
     */
    public fun asCharArray(): CharArray = asString().toCharArray()

    override fun toString(): String = "Password(password=${asString()})"

    /**
     * Clears the data, setting all bytes to 0.
     * Once [clear] has been called, the password will no longer be in memory.
     */
    public fun clear() {
        data.fill(0)
    }
}
