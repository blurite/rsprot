package net.rsprot.protocol.game.outgoing.inv

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.common.RSProtFlags
import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.common.game.outgoing.inv.internal.Inventory
import net.rsprot.protocol.common.game.outgoing.inv.internal.InventoryPool
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * Update inv partial is used to send an update of an inventory
 * that doesn't include the entire inventory.
 * This is typically used after the first [UpdateInvFull]
 * update has been performed, as subsequent updates tend to
 * be smaller, such as picking up an object - only
 * a single slot in the player's inventory would change, not warranting
 * the full 28-slot update as a result.
 *
 * A general rule of thumb for when to use partial updates is
 * if the percentage of modified objects is less than two thirds
 * of that of the highest modified index.
 * So, if our inventory has a capacity of 1,000, and we have sparsely
 * modified 500 indices throughout that, including the 999th index,
 * it is more beneficial to use the partial inventory update,
 * as the total bandwidth used by it is generally going to be less
 * than what the full update would require.
 * If, however, there is a continuous sequence of indices that
 * have been modified, such as everything from indices 0 to 100,
 * it is more beneficial to use the full update and set the
 * capacity to 100 in that.
 *
 * Below is a percentage-breakdown of how much more bandwidth the partial update
 * requires per object basis given different criteria, compared to the full update:
 * ```
 * 14.3% if slot < 128 && count >= 255
 * 28.6% if slot >= 128 && count >= 255
 * 33.3% if slot < 128 && count < 255
 * 66.6% if slot >= 128 && count < 255
 * ```
 *
 * While it is impossible to truly estimate what the exact threshold is,
 * this provides a good general idea of when to use either packet.
 *
 * @property combinedId the combined id of the interface and the component id.
 * For IF3-type interfaces, only negative values are allowed.
 * If one wishes to make the inventory a "mirror", e.g. for trading,
 * how both the player's own and the partner's inventory share the id,
 * a value of < -70000 is expected, this tells the client that the respective
 * inventory is a "mirrored" one.
 * For normal IF3 interfaces, a value of -1 is perfectly acceptable.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the IF1 interface on which the inventory lies.
 * For IF3 interfaces, no [interfaceId] should be provided.
 * @property componentId the component on which the inventory lies
 * @property inventoryId the id of the inventory to update
 * @property count the number of items added into this partial update.
 */
public class UpdateInvPartial private constructor(
    public val combinedId: Int,
    private val _inventoryId: UShort,
    private val inventory: Inventory,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        inventoryId: Int,
        provider: IndexedObjectProvider,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        inventoryId.toUShort(),
        buildInventory(provider),
    )

    public constructor(
        combinedId: Int,
        inventoryId: Int,
        provider: IndexedObjectProvider,
    ) : this(
        CombinedId(combinedId).combinedId,
        inventoryId.toUShort(),
        buildInventory(provider),
    )

    public constructor(
        inventoryId: Int,
        provider: IndexedObjectProvider,
    ) : this(
        -1,
        inventoryId.toUShort(),
        buildInventory(provider),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val inventoryId: Int
        get() = _inventoryId.toInt()
    public val count: Int
        get() = inventory.count
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    /**
     * Gets the obj in the [slot] provided.
     * @param slot the slot in the inventory.
     * @return the inventory object that's in that slot,
     * or [InventoryObject.NULL] if there's no object.
     * @throws IndexOutOfBoundsException if the [slot] is outside
     * the inventory's boundaries.
     */
    public fun getObject(slot: Int): InventoryObject = inventory[slot]

    public fun returnInventory() {
        inventory.clear()
        InventoryPool.pool.returnObject(inventory)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvPartial

        if (combinedId != other.combinedId) return false
        if (_inventoryId != other._inventoryId) return false
        if (inventory != other.inventory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _inventoryId.hashCode()
        result = 31 * result + inventory.hashCode()
        return result
    }

    override fun toString(): String =
        "UpdateInvPartial(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "inventoryId=$inventoryId, " +
            "count=$count" +
            ")"

    /**
     * An object provider interface is used to acquire the objs
     * that exist in different inventories. These objs are bit-packed
     * into a long, which gets further placed into a long array.
     * This is all in order to avoid garbage creation with inventories,
     * as this can be a considerable hot-spot for that.
     */
    public abstract class IndexedObjectProvider(
        internal val indices: Iterator<Int>,
    ) {
        /**
         * Provides an [InventoryObject] instance for a given slot
         * in inventory. If there is no object in that slot,
         * use [InventoryObject.NULL] as an indicator of it.
         */
        public abstract fun provide(slot: Int): InventoryObject
    }

    private companion object {
        /**
         * Builds an inventory based on a [provider].
         * @param provider the object provider, used to return information
         * about an object in a slot of an inventory.
         * @return an inventory object, which is a compressed representation
         * of a list of [InventoryObject]s, backed by a long array.
         */
        private fun buildInventory(provider: IndexedObjectProvider): Inventory {
            val inventory = InventoryPool.pool.borrowObject()
            for (index in provider.indices) {
                val obj = provider.provide(index)
                if (RSProtFlags.inventoryObjCheck) {
                    check(obj != InventoryObject.NULL) {
                        "Obj cannot be InventoryObject.NULL for partial updates. Use InventoryObject(slot, -1, -1) " +
                            "instead."
                    }
                    check(obj.slot >= 0) {
                        "Obj slot cannot be below zero: $obj $ $index"
                    }
                    check(obj.id == -1 || obj.count >= 0) {
                        "Obj count cannot be below zero: $obj @ $index"
                    }
                }
                inventory.add(obj)
            }
            return inventory
        }
    }
}
