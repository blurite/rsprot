package net.rsprot.protocol.common.game.outgoing.codec.zone.payload

import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * ZoneProt is a special type of outgoing message, used for any zone payload packet,
 * such as area sound or map proj anim.
 */
public interface ZoneProt : OutgoingGameMessage {
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

    public companion object {
        public const val LOC_ADD_CHANGE: Int = 0
        public const val LOC_DEL: Int = 1
        public const val LOC_ANIM: Int = 2
        public const val LOC_MERGE: Int = 3
        public const val OBJ_ADD: Int = 4
        public const val OBJ_DEL: Int = 5
        public const val OBJ_COUNT: Int = 6
        public const val OBJ_OPFILTER: Int = 7
        public const val MAP_ANIM: Int = 8
        public const val MAP_PROJANIM: Int = 9
        public const val SOUND_AREA: Int = 10
    }
}
