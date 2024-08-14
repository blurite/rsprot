package net.rsprot.protocol.api.suppliers

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarExceptionHandler
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityIndexSupplier

/**
 * The supplier for world entity info protocol, allowing the construction of the protocol and its
 * correct use.
 * @property worldEntityIndexSupplier the supplier for world entity indices, allowing the protocol
 * to determine what world entities need to be added to the high resolution view.
 * The server is expected to return all world entities, even ones that are already tracked as
 * the server has no way of determining what is already tracked.
 * @property worldEntityInfoProtocolWorker the worker behind the world entity info protocol, responsible
 * for executing the underlying tasks, either on a single thread or a thread pool.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class WorldEntityInfoSupplier
    @JvmOverloads
    public constructor(
        public val worldEntityIndexSupplier: WorldEntityIndexSupplier =
            WorldEntityIndexSupplier { _, _, _, _, _ ->
                emptySequence<Int>()
                    .iterator()
            },
        public val worldEntityAvatarExceptionHandler: WorldEntityAvatarExceptionHandler =
            WorldEntityAvatarExceptionHandler { index, exception ->
                logger.error(exception) {
                    "Exception in world entity avatar processing for index $index"
                }
            },
        public val worldEntityInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    ) {
        private companion object {
            private val logger = InlineLogger()
        }
    }
