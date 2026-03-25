package net.rsprot.protocol.game.outgoing.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.friendchat.MessageFriendChannel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageFriendChannelEncoder(
    private val huffmanCodecProvider: HuffmanCodecProvider,
) : MessageEncoder<MessageFriendChannel> {
    override val prot: ServerProt = GameServerProt.MESSAGE_FRIENDCHANNEL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MessageFriendChannel,
    ) {
        buffer.pjstr(message.sender)
        buffer.p8(message.channelNameBase37)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        buffer.p1(message.chatCrownType)
        val huffman = huffmanCodecProvider.provide()
        huffman.encode(buffer, message.message)
    }
}
