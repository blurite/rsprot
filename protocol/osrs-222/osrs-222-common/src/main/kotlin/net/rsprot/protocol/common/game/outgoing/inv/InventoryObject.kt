package net.rsprot.protocol.common.game.outgoing.inv

/**
 * Inventory objects are a value class around the primitive 'long'
 * to efficiently compress an inventory's contents into a long array.
 * This allows us to avoid any garbage creation that would otherwise
 * be created by making lists of objs repeatedly.
 */
@JvmInline
public value class InventoryObject(public val packed: Long) {
    public constructor(
        slot: Int,
        id: Int,
        count: Int,
    ) : this(
        (slot.toLong() and 0xFFFF)
            .or((id.toLong() and 0xFFFF) shl 16)
            .or((count.toLong() and 0xFFFFFFFF) shl 32),
    )

    public constructor(
        id: Int,
        count: Int,
    ) : this(
        0,
        id,
        count,
    )

    public val slot: Int
        get() {
            val value = (packed and 0xFFFF).toInt()
            return if (value == 0xFFFF) {
                -1
            } else {
                value
            }
        }
    public val id: Int
        get() {
            val value = (packed ushr 16 and 0xFFFF).toInt()
            return if (value == 0xFFFF) {
                -1
            } else {
                value
            }
        }
    public val count: Int
        get() = (packed ushr 32).toInt()

    override fun toString(): String {
        return "InventoryObject(" +
            "slot=$slot, " +
            "id=$id, " +
            "count=$count" +
            ")"
    }

    public companion object {
        public val NULL: InventoryObject = InventoryObject(-1)
    }
}
