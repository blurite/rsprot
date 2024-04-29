package net.rsprot.protocol.message

/**
 * ZoneProt is a special type of outgoing message, used for any zone payload packet,
 * such as area sound or map proj anim.
 */
public interface ZoneProt {
    /**
     * Prot id is a constant value assigned to each unique prot,
     * with the intent of being able to switch on these constants
     * and make use of a tableswitch operation, allowing fast O(1)
     * lookups for various zone prots. The respective, unique constants
     * are defined in the [Companion] object.
     * Each id is expected to be unique and incrementing.
     * Gaps should not exist as they cause the JVM to use a lookupswitch instead.
     */
    public val protId: Int
}
