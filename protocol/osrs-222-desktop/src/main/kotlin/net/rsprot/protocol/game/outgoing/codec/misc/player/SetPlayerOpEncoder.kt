package net.rsprot.protocol.game.outgoing.codec.misc.player

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.SetPlayerOp
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class SetPlayerOpEncoder : MessageEncoder<SetPlayerOp> {
    override val prot: ServerProt = GameServerProt.SET_PLAYER_OP

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: SetPlayerOp,
    ) {
        buffer.p1Alt2(message.id)
        buffer.p1Alt1(if (message.priority) 1 else 0)
        buffer.pjstr(message.op ?: "null")
    }
}
