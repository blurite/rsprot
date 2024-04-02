package net.rsprot.protocol.game.outgoing.codec.playerinfo

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class PlayerInfoEncoder : MessageEncoder<PlayerInfo> {
    override val prot: ServerProt = GameServerProt.PLAYER_INFO

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: PlayerInfo,
    ) {
        val backingBuffer = message.backingBuffer()
        try {
            buffer.buffer.writeBytes(backingBuffer)
        } finally {
            backingBuffer.release()
        }
    }
}
