package net.rsprot.protocol.game.outgoing.inv

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.internal.game.outgoing.inv.internal.Inventory
import net.rsprot.protocol.internal.game.outgoing.inv.internal.InventoryPool
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * Update inv full is used to perform a full synchronization of an inventory's
 * contents to the client.
 * The client will wipe any existing cache of this inventory prior to performing
 * an update.
 * While not very well known, it is possible to send less objs than the inventory's
 * respective capacity in the cache. As an example, if the inventory's capacity
 * in the cache is 500, but the inv only has a single object at the first slot,
 * a simple compression method is to send the capacity as 1 to the client,
 * and only inform of the single object that does exist - all others would be
 * presumed non-existent. There is no need to transmit all 500 slots when
 * the remaining 499 are not filled, saving considerable amount of space in the
 * process.
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
 * @property capacity the capacity of the inventory being transmitted in this
 * update.
 */
public class UpdateInvFull private constructor(
    public val combinedId: Int,
    private val _inventoryId: UShort,
    private val inventory: Inventory,
) : OutgoingGameMessage {
    @Deprecated(message = "Interface Id/Component Id are no longer supported by the client, guaranteed crashing.")
    public constructor(
        interfaceId: Int,
        componentId: Int,
        inventoryId: Int,
        capacity: Int,
        provider: ObjectProvider,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        inventoryId.toUShort(),
        buildInventory(capacity, provider),
    )

    public constructor(
        combinedId: Int,
        inventoryId: Int,
        capacity: Int,
        provider: ObjectProvider,
    ) : this(
        CombinedId(combinedId).combinedId,
        inventoryId.toUShort(),
        buildInventory(capacity, provider),
    ) {
        require(combinedId < 0) {
            "Positive combined id will always lead to crashing as the client no longer supports it."
        }
    }

    public constructor(
        inventoryId: Int,
        capacity: Int,
        provider: ObjectProvider,
    ) : this(
        -1,
        inventoryId.toUShort(),
        buildInventory(capacity, provider),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val inventoryId: Int
        get() = _inventoryId.toInt()
    public val capacity: Int
        get() = inventory.count
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    /**
     * Gets the bitpacked obj in the [slot] provided.
     * @param slot the slot in the inventory.
     * @return the inventory object that's in that slot,
     * or [InventoryObject.NULL] if there's no object.
     * @throws IndexOutOfBoundsException if the [slot] is outside
     * the inventory's boundaries.
     */
    public fun getObject(slot: Int): Long = inventory[slot]

    public fun returnInventory() {
        inventory.clear()
        net.rsprot.protocol.internal.game.outgoing.inv.internal.InventoryPool.pool.returnObject(inventory)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvFull

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
        "UpdateInvFull(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "inventoryId=$inventoryId, " +
            "capacity=$capacity" +
            ")"

    /**
     * An object provider interface is used to acquire the objs
     * that exist in different inventories. These objs are bit-packed
     * into a long, which gets further placed into a long array.
     * This is all in order to avoid garbage creation with inventories,
     * as this can be a considerable hot-spot for that.
     */
    public fun interface ObjectProvider {
        /**
         * Provides an [InventoryObject] for a given slot
         * in inventory. If there is no object in that slot,
         * use [InventoryObject.NULL] as an indicator of it.
         */
        public fun provide(slot: Int): Long
    }

    private companion object {
        /**
         * Builds an inventory based on a [provider].
         * @param capacity the capacity of the inventory, this is how far
         * the function will iterate to slots wise.
         * @param provider the object provider, used to return information
         * about an object in a slot of an inventory.
         * @return an inventory object, which is a compressed representation
         * of a list of [InventoryObject]s as longs, backed by a long array.
         */
        private fun buildInventory(
            capacity: Int,
            provider: ObjectProvider,
        ): Inventory {
            val inventory = net.rsprot.protocol.internal.game.outgoing.inv.internal.InventoryPool.pool.borrowObject()
            for (i in 0..<capacity) {
                val obj = provider.provide(i)
                if (net.rsprot.protocol.internal.RSProtFlags.inventoryObjCheck) {
                    check(obj == InventoryObject.NULL || InventoryObject.getCount(obj) >= 0) {
                        "Obj count cannot be below zero: $obj @ $i"
                    }
                }
                inventory.add(obj)
            }
            return inventory
        }
    }
}
