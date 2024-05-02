package net.rsprot.protocol

/**
 * A server prot category interface.
 * @property id the id of the prot category.
 * The values are intended to start with 0, which implies highest priority, and must
 * increment in ascending order.
 */
public interface ServerProtCategory {
    public val id: Int
}
