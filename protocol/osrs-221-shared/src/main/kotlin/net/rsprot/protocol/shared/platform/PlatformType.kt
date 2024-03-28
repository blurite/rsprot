package net.rsprot.protocol.shared.platform

import net.rsprot.protocol.platform.Platform

public enum class PlatformType(override val id: Int) : Platform {
    /**
     * The desktop platforms.
     * As the protocol is the same between the Java and C++ versions of desktop,
     * we use the same platform type for both here.
     */
    DESKTOP(0),

    /**
     * The Android platform for any devices running Android.
     */
    ANDROID(1),

    /**
     * The iOS platform for Apple devices.
     */
    IOS(2),
    ;

    public companion object {
        /**
         * The number of platform types that exist.
         * This number should be large enough to be used as array capacity,
         * as our buffers are often cached per-platform type, and we need to use
         * platform types as the array index.
         */
        public const val COUNT: Int = 3
    }
}
