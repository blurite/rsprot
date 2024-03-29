package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class Chat(
    encoders: Array<PrecomputedExtendedInfoEncoder<Chat>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<Chat, PrecomputedExtendedInfoEncoder<Chat>>(encoders) {
    public var colour: UByte = 0u
    public var effects: UByte = 0u
    public var modicon: UByte = 0u
    public var autotyper: Boolean = false
    public var text: String? = null
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
