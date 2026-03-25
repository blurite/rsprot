package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.MessageClanChannel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageClanChannelEncoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageEncoder<MessageClanChannel> {
    override val prot: ServerProt = GameServerProt.MESSAGE_CLANCHANNEL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MessageClanChannel,
    ) {
        buffer.p1(message.clanType)
        buffer.pjstr(message.name)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        buffer.p1(message.chatCrownType)
        val huffman = huffmanCodecProvider.provide()
        huffman.encode(buffer, message.message)
    }
}
