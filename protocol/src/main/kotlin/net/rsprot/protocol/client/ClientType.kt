package net.rsprot.protocol.client

/**
 * Client types are used to distinguish between various devices and
 * their differences in protocol scrambling. Each distinct client
 * needs its own decoders and encoders, though the models are shared between them.
 */
public interface ClientType {
    /**
     * The id of the client.
     * These ids should be incrementing, starting from zero.
     * The actual value is irrelevant, but this will frequently be used for
     * array accesses throughout the codebase.
     */
    public val id: Int
}
