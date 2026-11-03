package net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType

public interface WorldEntityResolutionChangeEncoder {
    public val clientType: OldSchoolClientType

    public fun encodeAddition(
        buffer: JagByteBuf,
        id: Int,
        sizeX: Int,
        sizeZ: Int,
        priority: Int,
    ): Int

    public fun rewriteExtendedInfoFlag(
        buffer: JagByteBuf,
        flagWriteIndex: Int,
        flag: Int,
    )
}
