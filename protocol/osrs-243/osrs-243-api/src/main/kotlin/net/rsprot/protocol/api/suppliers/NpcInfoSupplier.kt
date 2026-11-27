package net.rsprot.protocol.api.suppliers

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker

/**
 * The supplier for NPC info protocol, allowing the construction of the protocol and its
 * correct use.
 * @property npcAvatarExceptionHandler the exception handler for NPC avatars,
 * catching any exceptions that happen during pre-computations of NPC avatar blocks.
 * @property npcExtendedInfoFilter the filter for NPC extended info blocks, responsible
 * for ensuring that the NPC info packet never exceeds the 40 kilobyte limit.
 * @property npcInfoProtocolWorker the worker behind the NPC info protocol, responsible
 * for executing the underlying tasks, either on a single thread or a thread pool.
 */
public class NpcInfoSupplier
    @JvmOverloads
    public constructor(
        public val npcAvatarExceptionHandler: NpcAvatarExceptionHandler =
            NpcAvatarExceptionHandler { index, exception ->
                logger.error(exception) {
                    "Exception in processing npc avatar for npc $index"
                }
            },
        public val npcExtendedInfoFilter: ExtendedInfoFilter = DefaultExtendedInfoFilter(),
        public val npcInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NpcInfoSupplier

            if (npcExtendedInfoFilter != other.npcExtendedInfoFilter) return false
            if (npcInfoProtocolWorker != other.npcInfoProtocolWorker) return false
            if (npcAvatarExceptionHandler != other.npcAvatarExceptionHandler) return false

            return true
        }

        override fun hashCode(): Int {
            var result = npcExtendedInfoFilter.hashCode()
            result = 31 * result + npcInfoProtocolWorker.hashCode()
            result = 31 * result + npcAvatarExceptionHandler.hashCode()
            return result
        }

        override fun toString(): String =
            "NpcInfoSupplier(" +
                "npcAvatarExceptionHandler=$npcAvatarExceptionHandler, " +
                "npcExtendedInfoFilter=$npcExtendedInfoFilter, " +
                "npcInfoProtocolWorker=$npcInfoProtocolWorker" +
                ")"

        private companion object {
            private val logger = InlineLogger()
        }
    }
