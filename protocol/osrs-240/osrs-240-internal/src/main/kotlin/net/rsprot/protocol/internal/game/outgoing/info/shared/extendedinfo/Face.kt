package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The extended info block to make avatars face all the possibilities.
 * @param encoders the array of client-specific encoders for facing.
 */
public class Face(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Face>>,
) : TransientExtendedInfo<Face, PrecomputedExtendedInfoEncoder<Face>>() {
    /**
     * The index of the avatar to face-lock onto. For player avatars,
     * a value of 0x10000 is added onto the index to differentiate it.
     */
    public var index: Int = DEFAULT_VALUE
    public var walkMode: Int = 0
    public var kind: Kind = Kind.Reset
    public var instant: Boolean = false
    public var entityType: EntityType = EntityType.Npc
    public var entityFallbackAngle: Int = 0
    public var angle: Int = 0
    public var x: Int = 0
    public var z: Int = 0
    public var sizeX: Int = 1
    public var sizeZ: Int = 1

    public var outOfDate: Boolean = false
        private set

    public fun hasPersistentTarget(): Boolean {
        return kind == Kind.Entity
    }

    public fun markUpToDate() {
        if (!outOfDate) {
            return
        }
        outOfDate = false
        releaseBuffers()
    }

    public fun syncAngle(angle: Int) {
        this.outOfDate = true
        this.angle = angle
    }

    public enum class Kind(
        public val value: Int,
    ) {
        Entity(0),
        Loc(1),
        Angle(2),
        Reset(3),
    }

    public enum class EntityType(
        public val value: Int,
    ) {
        Npc(1),
        Player(2),
        WorldEntity(3),
    }

    public fun encode(buffer: JagByteBuf) {
        when (kind) {
            Kind.Reset -> {
                // no-op
            }
            Kind.Angle -> {
                buffer.pSmart1or2(angle)
            }
            Kind.Entity -> {
                buffer.pSmart1or2(entityType.value)
                buffer.pSmart2or4null(index)
                buffer.pSmart1or2(entityFallbackAngle)
            }
            Kind.Loc -> {
                buffer.pSmart1or2(x)
                buffer.pSmart1or2(z)
                val bitpackedSize = (sizeZ shl 4) or sizeX
                buffer.pSmart1or2(bitpackedSize)
            }
        }
    }

    override fun clear() {
        releaseBuffers()
        index = DEFAULT_VALUE
        walkMode = 0
        kind = Kind.Reset
        instant = false
        entityType = EntityType.Npc
        entityFallbackAngle = 0
        angle = 0
        x = 0
        z = 0
        sizeX = 1
        sizeZ = 1
        outOfDate = false
    }

    public companion object {
        public const val DEFAULT_VALUE: Int = -1
    }
}
