package net.rsprot.protocol.game.outgoing.codec.worldentity

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorld
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetActiveWorldEncoder : MessageEncoder<SetActiveWorld> {
    override val prot: ServerProt = GameServerProt.SET_ACTIVE_WORLD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: SetActiveWorld,
    ) {
        when (val type = message.worldType) {
            is SetActiveWorld.RootWorldType -> {
                // Prefix 0 implies a root world update
                buffer.p1(0)
                // The slot is ignored for root world updates
                buffer.p2(0)
                buffer.p1(type.activeLevel)
            }
            is SetActiveWorld.DynamicWorldType -> {
                // Prefix 1 implies a dynamic world update
                buffer.p1(1)
                buffer.p2(type.index)
                buffer.p1(type.activeLevel)
            }
        }
    }
}
