package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder.WorldEntityResolutionChangeEncoder

public class DesktopWorldEntityResolutionChangeEncoder : WorldEntityResolutionChangeEncoder {
    override val clientType: OldSchoolClientType = OldSchoolClientType.DESKTOP

    override fun encodeAddition(
        buffer: JagByteBuf,
        id: Int,
        sizeX: Int,
        sizeZ: Int,
        priority: Int,
    ): Int {
        buffer.p1((sizeX shl 4) or sizeZ)
        val flagWriteIndex = buffer.writerIndex()
        buffer.p1(0)
        buffer.p1Alt2(priority)
        buffer.p2Alt2(id)
        return flagWriteIndex
    }

    override fun rewriteExtendedInfoFlag(
        buffer: JagByteBuf,
        flagWriteIndex: Int,
        flag: Int,
    ) {
        val finalPosition = buffer.writerIndex()
        buffer.writerIndex(flagWriteIndex)
        buffer.p1(flag)
        buffer.writerIndex(finalPosition)
    }
}
