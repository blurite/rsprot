package net.rsprot.protocol.api.suppliers

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
public class WorldEntityInfoSupplier
    @JvmOverloads
    public constructor(
        public val worldEntityIndexSupplier: WorldEntityIndexSupplier,
        public val worldEntityAvatarExceptionHandler: WorldEntityAvatarExceptionHandler,
        public val worldEntityInfoProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    )
