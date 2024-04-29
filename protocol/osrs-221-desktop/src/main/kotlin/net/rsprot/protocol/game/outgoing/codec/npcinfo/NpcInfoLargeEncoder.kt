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
        val backingBuffer = message.buffer
        try {
            buffer.buffer.writeBytes(backingBuffer)
        } finally {
            backingBuffer.release()
        }
    }
}
