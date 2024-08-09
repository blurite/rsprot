package net.rsprot.protocol.loginprot.incoming.util

public enum class LoginClientType(
    public val id: Int,
) {
    DESKTOP(1),
    ANDROID(2),
    IOS(3),
    ENHANCED_WINDOWS(4),
    ENHANCED_MAC(5),
    ENHANCED_ANDROID(7),
    ENHANCED_IOS(8),
    ENHANCED_LINUX(10),
    ;

    public companion object {
        public operator fun get(id: Int): LoginClientType =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unknown client type: $id")
    }
}
