package net.rsprot.protocol.game.outgoing.codec.playerinfo

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoPacket
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class PlayerInfoEncoder : MessageEncoder<PlayerInfoPacket> {
    override val prot: ServerProt = GameServerProt.PLAYER_INFO

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: PlayerInfoPacket,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
