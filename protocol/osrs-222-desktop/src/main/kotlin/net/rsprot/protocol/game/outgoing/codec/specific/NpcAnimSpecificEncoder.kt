package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcAnimSpecificEncoder : MessageEncoder<NpcAnimSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_ANIM_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: NpcAnimSpecific,
    ) {
        buffer.p2(message.index)
        buffer.p2Alt2(message.id)
        buffer.p1Alt1(message.delay)
    }
}
