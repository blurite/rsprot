package net.rsprot.protocol.api.js5

import net.rsprot.protocol.js5.outgoing.Js5GroupResponse

public abstract class ByteBufJs5GroupProvider :
    Js5GroupProvider<Js5GroupProvider.ByteBufJs5GroupType> {
    override fun toJs5GroupResponse(
        input: Js5GroupProvider.ByteBufJs5GroupType,
        offset: Int,
        length: Int,
    ): Js5GroupResponse {
        return Js5GroupResponse(
            Js5GroupResponse.Js5ByteBufGroupResponse(
                input.buffer,
                offset,
                length,
            ),
        )
    }
}
