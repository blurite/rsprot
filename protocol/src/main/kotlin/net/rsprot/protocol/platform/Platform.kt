package net.rsprot.protocol.platform

/**
 * Platforms are used to distinguish between various devices and
 * their differences in protocol scrambling. Each distinct platform
 * needs its own decoders and encoders, though the models are shared between them.
 */
public interface Platform {
    /**
     * The id of the platform.
     * These ids should be incrementing, starting from zero.
     * The actual value is irrelevant, but this will frequently be used for
     * array accesses throughout the codebase.
     */
    public val id: Int
}
