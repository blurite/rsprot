package net.rsprot.protocol.internal.game.outgoing.inv.internal

import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.ObjectPool
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.SoftReferenceObjectPool

/**
 * A soft-reference based pool of [Inventory] objects, with the primary
 * intent being to avoid re-creating lists of objs which end up wasting
 * as much as 137kb of memory for a single inventory that's up to 5713 objs
 * in capacity. While it is unlikely that any inventory would get near that,
 * servers do commonly expand inventory capacities to numbers like 2,000 or 2,500,
 * which would still consume up 48-60kb of memory as a result in any traditional manner.
 *
 * Breakdown of the above statements:
 * Assuming an implementation where List<Obj> is provided to the respective packets,
 * where Obj is a class of three properties:
 *
 * ```
 * Slot: Int (necessary for partial inv updates)
 * Id: Int
 * Count: Int
 * ```
 *
 * The resulting memory requirement would be `(12 + (3 * 4))` bytes per obj.
 * While this does coincide with the memory alignment,
 * it still ends up consuming 24 bytes per obj, all of which would be discarded shortly after.
 * Given the assumption that 1,000 players log in at once, and they all have a bank
 * of 1000 objs - which is a fairly conservative estimate -, the resulting waste of memory
 * is 24 megabytes alone. All of this can be avoided through the use of an object pool,
 * as done below.
 */
public data object InventoryPool {
    public val pool: ObjectPool<Inventory> =
        SoftReferenceObjectPool(
            createFactory(),
        )

    private fun createFactory(): PooledObjectFactory<Inventory> {
        return object : BasePooledObjectFactory<Inventory>() {
            override fun create(): Inventory {
                // 5713 is the maximum theoretical number of objs an inventory can carry
                // before the 40kb limitation could get hit
                // This assumes each obj sends a quantity of >= 255
                return Inventory(5713)
            }

            override fun wrap(p0: Inventory): PooledObject<Inventory> = DefaultPooledObject(p0)
        }
    }
}
