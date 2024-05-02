package net.rsprot.protocol.api.suppliers

import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcIndexSupplier
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker

public class NpcInfoSupplier
    @JvmOverloads
    public constructor(
        public val npcIndexSupplier: NpcIndexSupplier,
        public val npcAvatarExceptionHandler: NpcAvatarExceptionHandler,
        public val npcExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
        public val npcInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NpcInfoSupplier

            if (npcIndexSupplier != other.npcIndexSupplier) return false
            if (npcExtendedInfoFilter != other.npcExtendedInfoFilter) return false
            if (npcInfoProtocolWorker != other.npcInfoProtocolWorker) return false
            if (npcAvatarExceptionHandler != other.npcAvatarExceptionHandler) return false

            return true
        }

        override fun hashCode(): Int {
            var result = npcIndexSupplier.hashCode()
            result = 31 * result + npcExtendedInfoFilter.hashCode()
            result = 31 * result + npcInfoProtocolWorker.hashCode()
            result = 31 * result + npcAvatarExceptionHandler.hashCode()
            return result
        }

        override fun toString(): String {
            return "NpcInfoSupplier(" +
                "npcIndexSupplier=$npcIndexSupplier, " +
                "npcAvatarExceptionHandler=$npcAvatarExceptionHandler, " +
                "npcExtendedInfoFilter=$npcExtendedInfoFilter, " +
                "npcInfoProtocolWorker=$npcInfoProtocolWorker" +
                ")"
        }
    }
