package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update stockmarket slot packet is used to set up
 * an offer on the Grand Exchange, or to clear out an
 * offer.
 * Unlike [UpdateStockMarketSlotV1], the price and the amount of gold
 * received are 64-bit values.
 * @property slot the Grand Exchange slot to update
 * @property update the update type to perform, either
 * [ResetStockMarketSlot] or [SetStockMarketSlot].
 */
public class UpdateStockMarketSlotV2 private constructor(
    private val _slot: UByte,
    public val update: StockMarketUpdateType,
) : OutgoingGameMessage {
    public constructor(
        slot: Int,
        update: StockMarketUpdateType,
    ) : this(
        slot.toUByte(),
        update,
    )

    public val slot: Int
        get() = _slot.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateStockMarketSlotV2

        return _slot == other._slot && update == other.update
    }

    override fun hashCode(): Int = 31 * _slot.hashCode() + update.hashCode()

    override fun toString(): String =
        "UpdateStockMarketSlotV2(" +
            "slot=$slot, " +
            "update=$update" +
            ")"

    public sealed interface StockMarketUpdateType

    // TODO(241): Figure out the remaining six bytes in the reset packet.
    // The native client only reads 28 of the 34 bytes, so this update cannot be encoded yet.
    public data object ResetStockMarketSlot : StockMarketUpdateType

    /**
     * Set stockmarket slot update creates an offer
     * on the Grand Exchange.
     * @property status the status of the offer to create.
     * @property obj the obj to set in the specified slot
     * @property price the price per item
     * @property count the count to buy or sell
     * @property completedCount the amount already bought or sold
     * @property completedGold the amount of gold received
     */
    public class SetStockMarketSlot private constructor(
        private val _status: Byte,
        private val _obj: UShort,
        public val price: Long,
        public val count: Int,
        public val completedCount: Int,
        public val completedGold: Long,
    ) : StockMarketUpdateType {
        public constructor(
            status: Int,
            obj: Int,
            price: Long,
            count: Int,
            completedCount: Int,
            completedGold: Long,
        ) : this(
            status.toByte(),
            obj.toUShort(),
            price,
            count,
            completedCount,
            completedGold,
        )

        public val status: Int
            get() = _status.toInt()
        public val obj: Int
            get() = _obj.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetStockMarketSlot

            if (_status != other._status) return false
            if (_obj != other._obj) return false
            if (price != other.price) return false
            if (count != other.count) return false
            if (completedCount != other.completedCount) return false
            if (completedGold != other.completedGold) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _status.toInt()
            result = 31 * result + _obj.hashCode()
            result = 31 * result + price.hashCode()
            result = 31 * result + count
            result = 31 * result + completedCount
            result = 31 * result + completedGold.hashCode()
            return result
        }

        override fun toString(): String =
            "SetStockMarketSlot(" +
                "status=$status, " +
                "obj=$obj, " +
                "price=$price, " +
                "count=$count, " +
                "completedCount=$completedCount, " +
                "completedGold=$completedGold" +
                ")"
    }
}
