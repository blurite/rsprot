package net.rsprot.protocol.common.client

import net.rsprot.protocol.client.ClientType

public enum class OldSchoolClientType(
    override val id: Int,
) : ClientType {
    /**
     * The desktop clients.
     * As the protocol is the same between the Java and C++ versions of desktop,
     * we use the same client type for both here.
     */
    DESKTOP(0),

    /**
     * The Android client.
     * This is a separate client type as the protocol differs from the desktop clients.
     */
    ANDROID(1),

    /**
     * The iOS client.
     * This is a separate client type as the protocol differs from the desktop clients.
     */
    IOS(2),
    ;

    public companion object {
        /**
         * The number of client types that exist.
         * This number should be large enough to be used as array capacity,
         * as our buffers are often cached per-client type, and we need to use
         * client types as the array index.
         */
        public const val COUNT: Int = 3
    }
}
