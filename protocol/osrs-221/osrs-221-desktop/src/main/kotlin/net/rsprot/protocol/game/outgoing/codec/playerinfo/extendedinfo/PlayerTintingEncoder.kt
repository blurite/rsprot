package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.PlayerTintingList

public class PlayerTintingEncoder : OnDemandExtendedInfoEncoder<PlayerTintingList> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedAvatarIndex: Int,
        extendedInfo: PlayerTintingList,
    ) {
        val tinting = extendedInfo[localPlayerIndex]
        buffer.p2Alt3(tinting.start.toInt())
        buffer.p2Alt1(tinting.end.toInt())
        buffer.p1Alt2(tinting.hue.toInt())
        buffer.p1(tinting.saturation.toInt())
        buffer.p1(tinting.lightness.toInt())
        buffer.p1Alt2(tinting.weight.toInt())
    }
}
