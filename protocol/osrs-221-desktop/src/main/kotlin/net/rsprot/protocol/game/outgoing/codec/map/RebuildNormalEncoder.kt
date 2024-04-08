package net.rsprot.protocol.game.outgoing.codec.map

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.map.RebuildNormal
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildNormalEncoder : MessageEncoder<RebuildNormal> {
    override val prot: ServerProt = GameServerProt.REBUILD_NORMAL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: RebuildNormal,
    ) {
        buffer.p2Alt3(message.zoneX)
        buffer.p2Alt3(message.zoneZ)
        // Currently unused property, unknown what it is for, presumably sailing-related
        buffer.p2Alt2(0)
        buffer.p2(message.keys.size)
        for (xteaKey in message.keys) {
            for (intKey in xteaKey.key) {
                buffer.p4(intKey)
            }
        }
    }
}
