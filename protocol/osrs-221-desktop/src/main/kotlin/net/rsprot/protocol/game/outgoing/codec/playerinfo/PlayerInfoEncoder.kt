package net.rsprot.protocol.game.outgoing.codec.playerinfo

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
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
    ): JagByteBuf {
        return message
            .backingBuffer()
            .toJagByteBuf()
    }

    /**
     * Do not allocate any buffers here, as player info is pre-calculated earlier in the cycle.
     */
    override fun allocBuffer(allocator: ByteBufAllocator): JagByteBuf {
        return JagByteBuf.EMPTY_JAG_BUF
    }
}
