package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.ServerTickEnd
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ServerTickEndEncoder : MessageEncoder<ServerTickEnd> {
    override val prot: ServerProt = GameServerProt.SERVER_TICK_END

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: ServerTickEnd,
    ) {
        // Note: In Java, this packet does not have any 'body',
        // meaning it is the only packet that does not do anything
        // See this image: https://media.z-kris.com/2024/04/idea64_TC9RNRYaTS.png
    }
}
