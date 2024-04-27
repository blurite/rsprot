package net.rsprot.protocol.channel

import io.netty.util.AttributeKey
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.cryptography.StreamCipherPair
import net.rsprot.protocol.filter.MessageFilter

public object ChannelAttributes {
    public val STREAM_CIPHER_PAIR: AttributeKey<StreamCipherPair> =
        AttributeKey.newInstance("prot_stream_cipher_pair")

    public val GAME_MESSAGE_FILTER: AttributeKey<MessageFilter> =
        AttributeKey.newInstance("prot_game_message_filter")

    public val HUFFMAN_CODEC: AttributeKey<HuffmanCodec> =
        AttributeKey.newInstance("prot_huffman_codec")

    public val XOR_ENCRYPTION_KEY: AttributeKey<Int> =
        AttributeKey.newInstance("prot_js5_xor_encryption_key")
}
