package net.rsprot.protocol.loginprot.incoming.util

public enum class LoginPlatformType(
    public val id: Int,
) {
    DEFAULT(0),
    STEAM(1),
    ANDROID(2),
    APPLE(3),
    JAGEX(5),
    ;

    public companion object {
        public operator fun get(id: Int): LoginPlatformType {
            return entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unknown platform type: $id")
        }
    }
}
