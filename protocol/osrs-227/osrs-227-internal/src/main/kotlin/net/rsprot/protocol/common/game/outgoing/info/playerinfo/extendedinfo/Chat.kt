package net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The chat extended info block, responsible for any public messages.
 * @param encoders the array of client-specific encoders for chat.
 */
public class Chat(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Chat>>,
) : TransientExtendedInfo<Chat, PrecomputedExtendedInfoEncoder<Chat>>() {
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
     * The text itself to render. This will be compressed using the [net.rsprot.compression.HuffmanCodec].
     */
    public var text: String? = null

    /**
     * The colour pattern for specialized chat message colours,
     */
    public var pattern: ByteArray? = null

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
