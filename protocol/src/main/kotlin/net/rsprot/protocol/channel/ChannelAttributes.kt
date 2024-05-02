package net.rsprot.protocol.channel

import io.netty.util.AttributeKey
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.cipher.StreamCipherPair

public object ChannelAttributes {
    public val STREAM_CIPHER_PAIR: AttributeKey<StreamCipherPair> =
        AttributeKey.newInstance("prot_stream_cipher_pair")

    public val HUFFMAN_CODEC: AttributeKey<HuffmanCodecProvider> =
        AttributeKey.newInstance("prot_huffman_codec")

    public val XOR_ENCRYPTION_KEY: AttributeKey<Int> =
        AttributeKey.newInstance("prot_js5_xor_encryption_key")
}
