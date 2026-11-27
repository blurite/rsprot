package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update trading post packet was used to create
 * a list of offers of a specific obj in the trading
 * post interface back when it still existed, in circa
 * 2014. This packet has not had a use since then, however.
 * Unlike [UpdateTradingPostV1], the price of each offer is a 64-bit value.
 * @property updateType the update type to perform, either
 * [ResetTradingPost] or [SetTradingPostOfferList].
 */
public class UpdateTradingPostV2(
    public val updateType: TradingPostUpdateType,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun estimateSize(): Int {
        return when (updateType) {
            ResetTradingPost -> Byte.SIZE_BYTES
            is SetTradingPostOfferList -> {
                val sizePerOffer =
                    13 +
                        13 +
                        Short.SIZE_BYTES +
                        Long.SIZE_BYTES +
                        Long.SIZE_BYTES +
                        Int.SIZE_BYTES

                Byte.SIZE_BYTES +
                    Long.SIZE_BYTES +
                    Short.SIZE_BYTES +
                    Byte.SIZE_BYTES +
                    Short.SIZE_BYTES +
                    (updateType.offers.size * sizePerOffer)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateTradingPostV2

        return updateType == other.updateType
    }

    override fun hashCode(): Int = updateType.hashCode()

    override fun toString(): String = "UpdateTradingPostV2(updateType=$updateType)"

    public sealed interface TradingPostUpdateType

    public data object ResetTradingPost : TradingPostUpdateType

    public class SetTradingPostOfferList private constructor(
        public val age: Long,
        private val _obj: UShort,
        public val status: Boolean,
        public val offers: List<TradingPostOffer>,
    ) : TradingPostUpdateType {
        public constructor(
            age: Long,
            obj: Int,
            status: Boolean,
            offers: List<TradingPostOffer>,
        ) : this(
            age,
            obj.toUShort(),
            status,
            offers,
        ) {
            require(offers.size <= 65535) {
                "Offers size must fit in an unsigned short"
            }
        }

        public val obj: Int
            get() = _obj.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetTradingPostOfferList

            if (age != other.age) return false
            if (_obj != other._obj) return false
            if (status != other.status) return false
            if (offers != other.offers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = age.hashCode()
            result = 31 * result + _obj.hashCode()
            result = 31 * result + status.hashCode()
            result = 31 * result + offers.hashCode()
            return result
        }

        override fun toString(): String =
            "SetTradingPostOfferList(" +
                "age=$age, " +
                "obj=$obj, " +
                "status=$status, " +
                "offers=$offers" +
                ")"
    }

    public class TradingPostOffer private constructor(
        public val name: String,
        public val previousName: String,
        private val _world: UShort,
        public val time: Long,
        public val price: Long,
        public val count: Int,
    ) {
        public constructor(
            name: String,
            previousName: String,
            world: Int,
            time: Long,
            price: Long,
            count: Int,
        ) : this(
            name,
            previousName,
            world.toUShort(),
            time,
            price,
            count,
        )

        public val world: Int
            get() = _world.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TradingPostOffer

            if (name != other.name) return false
            if (previousName != other.previousName) return false
            if (_world != other._world) return false
            if (time != other.time) return false
            if (price != other.price) return false
            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + previousName.hashCode()
            result = 31 * result + _world.hashCode()
            result = 31 * result + time.hashCode()
            result = 31 * result + price.hashCode()
            result = 31 * result + count
            return result
        }

        override fun toString(): String =
            "TradingPostOffer(" +
                "name='$name', " +
                "previousName='$previousName', " +
                "world=$world, " +
                "time=$time, " +
                "price=$price, " +
                "count=$count" +
                ")"
    }
}
