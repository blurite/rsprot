package net.rsprot.protocol.game.outgoing.codec.worldentity

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.entityinfo.ClearEntities
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClearEntitiesEncoder : MessageEncoder<ClearEntities> {
    override val prot: ServerProt = GameServerProt.CLEAR_ENTITIES

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: ClearEntities,
    ) {
    }
}
