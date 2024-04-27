package net.rsprot.protocol.api.js5

import net.rsprot.protocol.js5.outgoing.Js5GroupResponse

public abstract class RandomAccessFileJs5GroupProvider :
    Js5GroupProvider<Js5GroupProvider.RandomAccessFileJs5GroupType> {
    override fun toJs5GroupResponse(
        input: Js5GroupProvider.RandomAccessFileJs5GroupType,
        offset: Int,
        length: Int,
    ): Js5GroupResponse {
        return Js5GroupResponse(
            Js5GroupResponse.Js5FileGroupResponse(
                input.raf,
                offset,
                length,
            ),
        )
    }
}
