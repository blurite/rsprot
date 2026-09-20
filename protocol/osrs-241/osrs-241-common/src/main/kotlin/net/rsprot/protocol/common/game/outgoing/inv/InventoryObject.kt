package net.rsprot.protocol.common.game.outgoing.inv

/**
 * Inventory object is a helper object built around the primitive 'long' type.
 * We utilize longs directly here to reduce the effects of garbage creation
 * via inventories, as this can otherwise get quite severe with a lot of players.
 */
public object InventoryObject {
    public const val NULL: Long = -1

    @JvmSynthetic
    public operator fun invoke(
        id: Int,
        count: Int,
    ): Long = pack(0, id, count)

    @JvmSynthetic
    public operator fun invoke(
        slot: Int,
        id: Int,
        count: Int,
    ): Long = pack(slot, id, count)

    @JvmStatic
    public fun pack(
        id: Int,
        count: Int,
    ): Long = pack(0, id, count)

    @JvmStatic
    public fun pack(
        slot: Int,
        id: Int,
        count: Int,
    ): Long =
        (slot.toLong() and 0xFFFF)
            .or((id.toLong() and 0xFFFF) shl 16)
            .or((count.toLong() and 0xFFFFFFFF) shl 32)

    @JvmStatic
    public fun getSlot(packed: Long): Int {
        val value = (packed and 0xFFFF).toInt()
        return if (value == 0xFFFF) {
            -1
        } else {
            value
        }
    }

    @JvmStatic
    public fun getId(packed: Long): Int {
        val value = (packed ushr 16 and 0xFFFF).toInt()
        return if (value == 0xFFFF) {
            -1
        } else {
            value
        }
    }

    @JvmStatic
    public fun getCount(packed: Long): Int = (packed ushr 32).toInt()
}
