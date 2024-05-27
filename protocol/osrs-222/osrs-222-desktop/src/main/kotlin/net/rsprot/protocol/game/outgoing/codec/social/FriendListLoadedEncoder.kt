package net.rsprot.protocol.game.outgoing.codec.social

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.FriendListLoaded
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class FriendListLoadedEncoder : MessageEncoder<FriendListLoaded> {
    override val prot: ServerProt = GameServerProt.FRIENDLIST_LOADED

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: FriendListLoaded,
    ) {
    }
}
