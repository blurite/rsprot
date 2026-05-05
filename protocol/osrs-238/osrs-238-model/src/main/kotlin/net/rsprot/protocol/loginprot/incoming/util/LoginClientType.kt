package net.rsprot.protocol.loginprot.incoming.util

import net.rsprot.protocol.common.client.OldSchoolClientType

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

    public fun toOldSchoolClientType(): OldSchoolClientType? {
        return when (this) {
            DESKTOP -> OldSchoolClientType.DESKTOP
            ENHANCED_WINDOWS -> OldSchoolClientType.DESKTOP
            ENHANCED_LINUX -> OldSchoolClientType.DESKTOP
            ENHANCED_MAC -> OldSchoolClientType.DESKTOP
            ENHANCED_ANDROID -> OldSchoolClientType.ANDROID
            ENHANCED_IOS -> OldSchoolClientType.IOS
            else -> null
        }
    }

    public companion object {
        public operator fun get(id: Int): LoginClientType =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unknown client type: $id")
    }
}
