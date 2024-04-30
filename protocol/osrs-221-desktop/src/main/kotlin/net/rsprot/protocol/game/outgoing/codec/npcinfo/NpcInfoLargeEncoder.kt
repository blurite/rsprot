package net.rsprot.protocol.game.outgoing.codec.npcinfo

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoLarge
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcInfoLargeEncoder : MessageEncoder<NpcInfoLarge> {
    override val prot: ServerProt = GameServerProt.NPC_INFO_LARGE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: NpcInfoLarge,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
