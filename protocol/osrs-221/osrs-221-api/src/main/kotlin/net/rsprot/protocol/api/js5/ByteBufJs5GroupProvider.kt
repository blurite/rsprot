package net.rsprot.protocol.api.js5

import net.rsprot.protocol.js5.outgoing.Js5GroupResponse

/**
 * A JS5 group provider of ByteBuf instances.
 */
public abstract class ByteBufJs5GroupProvider : Js5GroupProvider<Js5GroupProvider.ByteBufJs5GroupType> {
    override fun toJs5GroupResponse(
        input: Js5GroupProvider.ByteBufJs5GroupType,
        offset: Int,
        length: Int,
        key: Int,
    ): Js5GroupResponse =
        Js5GroupResponse(
            Js5GroupResponse.Js5ByteBufGroupResponse(
                input.buffer,
                offset,
                length,
                key,
            ),
        )
}
