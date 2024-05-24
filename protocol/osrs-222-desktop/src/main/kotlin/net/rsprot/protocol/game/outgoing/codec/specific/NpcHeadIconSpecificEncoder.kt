package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcHeadIconSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcHeadIconSpecificEncoder : MessageEncoder<NpcHeadIconSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: NpcHeadIconSpecific,
    ) {
        buffer.p1Alt2(message.headIconSlot)
        buffer.p4Alt2(message.spriteGroup)
        buffer.p2Alt3(message.spriteIndex)
        buffer.p2(message.index)
    }
}
