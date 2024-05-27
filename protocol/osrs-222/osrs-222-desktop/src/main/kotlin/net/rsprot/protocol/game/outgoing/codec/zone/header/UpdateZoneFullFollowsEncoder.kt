package net.rsprot.protocol.game.outgoing.codec.zone.header

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZoneFullFollows
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateZoneFullFollowsEncoder : MessageEncoder<UpdateZoneFullFollows> {
    override val prot: ServerProt = GameServerProt.UPDATE_ZONE_FULL_FOLLOWS

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateZoneFullFollows,
    ) {
        buffer.p1Alt1(message.zoneX)
        buffer.p1Alt3(message.level)
        buffer.p1(message.zoneZ)
    }
}
