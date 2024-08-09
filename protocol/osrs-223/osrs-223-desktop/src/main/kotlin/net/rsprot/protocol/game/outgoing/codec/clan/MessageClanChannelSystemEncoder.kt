package net.rsprot.protocol.game.outgoing.codec.clan

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.MessageClanChannelSystem
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageClanChannelSystemEncoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageEncoder<MessageClanChannelSystem> {
    override val prot: ServerProt = GameServerProt.MESSAGE_CLANCHANNEL_SYSTEM

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MessageClanChannelSystem,
    ) {
        buffer.p1(message.clanType)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        val huffman = huffmanCodecProvider.provide()
        huffman.encode(buffer, message.message)
    }
}
