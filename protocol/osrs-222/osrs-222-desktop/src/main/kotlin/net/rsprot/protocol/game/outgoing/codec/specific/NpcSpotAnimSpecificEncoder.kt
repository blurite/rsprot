package net.rsprot.protocol.game.outgoing.codec.specific

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcSpotAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcSpotAnimSpecificEncoder : MessageEncoder<NpcSpotAnimSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_SPOTANIM_SPECIFIC

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: NpcSpotAnimSpecific,
    ) {
        buffer.p4Alt1((message.height shl 16) or message.delay)
        buffer.p1Alt2(message.slot)
        buffer.p2Alt3(message.index)
        buffer.p2(message.id)
    }
}
