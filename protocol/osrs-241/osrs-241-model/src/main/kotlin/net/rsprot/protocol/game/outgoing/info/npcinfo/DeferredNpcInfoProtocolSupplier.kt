package net.rsprot.protocol.game.outgoing.info.npcinfo

/**
 * A supplier for NPC info protocol to get around a circular dependency.
 * Rather than re-design a significant portion of the classes around NPC info (and deal
 * with backporting differences to previous revisions), it's easier to just use
 * a supplier to get around the fact.
 *
 * @property protocol the backing protocol property, initialized lazily.
 */
public class DeferredNpcInfoProtocolSupplier {
    private lateinit var protocol: NpcInfoProtocol

    /**
     * Supplies the protocol value to this supplier.
     * @param protocol the protocol value to assign.
     */
    public fun supply(protocol: NpcInfoProtocol) {
        this.protocol = protocol
    }

    /**
     * Gets the previously supplied protocol value, or throws an exception if it has not been supplied.
     */
    public fun get(): NpcInfoProtocol = protocol
}
