package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.PlayerAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class PlayerAnimSpecificEncoder : MessageEncoder<PlayerAnimSpecific> {
    override val prot: ServerProt = GameServerProt.PLAYER_ANIM_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: PlayerAnimSpecific,
    ) {
        buffer.p1Alt1(message.delay)
        buffer.p2(message.id)
    }
}
