package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.outgoing.info.extendedinfo.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.TintingList

public class TintingEncoder : ExtendedInfoEncoder<TintingList> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedPlayerIndex: Int,
        extendedInfo: TintingList,
    ) {
        val tinting = extendedInfo[localPlayerIndex]
        buffer.p2Alt3(tinting.startTime.toInt())
        buffer.p2Alt1(tinting.endTime.toInt())
        buffer.p1Alt2(tinting.hue.toInt())
        buffer.p1(tinting.saturation.toInt())
        buffer.p1(tinting.luminance.toInt())
        buffer.p1(tinting.opacity.toInt())
    }
}
