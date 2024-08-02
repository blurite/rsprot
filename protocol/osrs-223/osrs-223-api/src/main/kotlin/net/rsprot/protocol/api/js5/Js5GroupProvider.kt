package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf

/**
 * The group provider interface for JS5.
 */
public fun interface Js5GroupProvider {
    /**
     * Provides a JS5 group based on the input archive and group
     * @param archive the archive id requested by the client
     * @param group the group in that archive requested
     * @return a full JS5 group to be written to the client
     */
    public fun provide(
        archive: Int,
        group: Int,
    ): ByteBuf
}
