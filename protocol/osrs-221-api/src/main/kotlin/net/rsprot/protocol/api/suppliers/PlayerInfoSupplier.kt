package net.rsprot.protocol.api.suppliers

import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker

public class PlayerInfoSupplier
    @JvmOverloads
    public constructor(
        public val playerExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
        public val playerInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PlayerInfoSupplier

            if (playerExtendedInfoFilter != other.playerExtendedInfoFilter) return false
            if (playerInfoProtocolWorker != other.playerInfoProtocolWorker) return false

            return true
        }

        override fun hashCode(): Int {
            var result = playerExtendedInfoFilter.hashCode()
            result = 31 * result + playerInfoProtocolWorker.hashCode()
            return result
        }

        override fun toString(): String {
            return "PlayerInfoSupplier(" +
                "playerExtendedInfoFilter=$playerExtendedInfoFilter, " +
                "playerInfoProtocolWorker=$playerInfoProtocolWorker" +
                ")"
        }
    }
