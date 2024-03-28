package net.rsprot.protocol.game.outgoing.info.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo

public interface ExtendedInfoEncoder<in T : ExtendedInfo> {
    public fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedPlayerIndex: Int,
        extendedInfo: T,
    )
}
