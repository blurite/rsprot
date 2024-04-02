package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The chat extended info block, responsible for any public messages.
 * @param encoders the array of platform-specific encoders for appearance.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing the [text] property.
 */
public class Chat(
    encoders: Array<PrecomputedExtendedInfoEncoder<Chat>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<Chat, PrecomputedExtendedInfoEncoder<Chat>>(encoders) {
    /**
     * The colour to apply to this chat message.
     */
    public var colour: UByte = 0u

    /**
     * The effect to apply to this chat message.
     */
    public var effects: UByte = 0u

    /**
     * The mod icon to render next to the name of the avatar who said this message.
     */
    public var modicon: UByte = 0u

    /**
     * Whether this avatar is using the built-in autotyper mechanic.
     */
    public var autotyper: Boolean = false

    /**
     * The text itself to render. This will be compressed using the [huffmanCodec].
     */
    public var text: String? = null

    /**
     * The colour pattern for specialized chat message colours,
     */
    public var pattern: ByteArray? = null

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        colour = 0u
        effects = 0u
        modicon = 0u
        autotyper = false
        text = null
        pattern = null
    }
}
