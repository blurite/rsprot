package net.rsprot.protocol.common.game.outgoing.inv.internal

import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import kotlin.jvm.Throws

/**
 * A compressed internal representation of an inventory, to be transmitted
 * with the various inventory update packets.
 * Rather than use a List<Obj>, we pool these [Inventory] instances to avoid
 * generating significant amounts of garbage.
 * For a popular server, it is perfectly reasonable to expect north of a gigabyte
 * of memory to be wasted through List<Obj> instances in the span of an hour.
 * We eliminate all garbage generation by using soft-reference pooled inventory
 * objects. While this does result in a small hit due to the synchronization involved,
 * it is nothing compared to the hit caused by garbage collection and memory allocation
 * involved with inventories.
 *
 * @property count the current count of objs in this inventory
 * @property contents the array of contents of this inventory.
 * The contents array is initialized at the maximum theoretical size
 * of the full inv update packet.
 */
public class Inventory private constructor(
    public var count: Int,
    private val contents: LongArray,
) {
    public constructor(
        capacity: Int,
    ) : this(
        0,
        LongArray(capacity),
    )

    /**
     * Adds an obj into this inventory
     * @param obj the obj to be added to this inventory
     * @throws ArrayIndexOutOfBoundsException if the inventory is full
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun add(obj: InventoryObject) {
        contents[count++] = obj.packed
    }

    /**
     * Gets the obj in [slot].
     * @return the obj in the respective slot, or [InventoryObject.NULL]
     * if no object exists in that slot.
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public operator fun get(slot: Int): InventoryObject {
        return InventoryObject(contents[slot])
    }

    /**
     * Clears the inventory by setting the count to zero.
     * The actual backing long array can remain filled with values,
     * as those will be overridden by real usages whenever necessary.
     */
    public fun clear() {
        count = 0
    }
}
