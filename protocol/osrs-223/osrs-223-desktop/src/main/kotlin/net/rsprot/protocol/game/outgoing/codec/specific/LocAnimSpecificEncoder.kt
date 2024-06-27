package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.LocAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class LocAnimSpecificEncoder : MessageEncoder<LocAnimSpecific> {
    override val prot: ServerProt = GameServerProt.LOC_ANIM_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LocAnimSpecific,
    ) {
        buffer.p3Alt1(message.coordInBuildAreaPacked)
        buffer.p1Alt2(message.locPropertiesPacked)
        buffer.p2Alt3(message.id)
    }
}
