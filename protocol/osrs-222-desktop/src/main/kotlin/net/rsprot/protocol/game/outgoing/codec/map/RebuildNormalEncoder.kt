package net.rsprot.protocol.game.outgoing.codec.map

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.map.RebuildLogin
import net.rsprot.protocol.game.outgoing.map.StaticRebuildMessage
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildNormalEncoder : MessageEncoder<StaticRebuildMessage> {
    override val prot: ServerProt = GameServerProt.REBUILD_NORMAL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: StaticRebuildMessage,
    ) {
        // We have to use the same encoder as it relies on the prot
        // under the hood to map the encoders down
        if (message is RebuildLogin) {
            val gpiInitBlock = message.gpiInitBlock
            try {
                buffer.buffer.writeBytes(
                    gpiInitBlock,
                    gpiInitBlock.readerIndex(),
                    gpiInitBlock.readableBytes(),
                )
            } finally {
                gpiInitBlock.release()
            }
        }
        buffer.p2Alt3(message.zoneZ)
        // Currently unused property, unknown what it is for, presumably sailing-related
        buffer.p2Alt3(0)
        buffer.p2Alt3(message.zoneX)
        buffer.p2(message.keys.size)
        for (xteaKey in message.keys) {
            for (intKey in xteaKey.key) {
                buffer.p4(intKey)
            }
        }
    }
}
