package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo

public class Chat : TransientExtendedInfo() {
    public var effects: UByte = 0u
    public var modicon: UByte = 0u
    public var autotyper: Boolean = false
    public var text: String? = null
    public var pattern: ByteArray? = null

    override fun clear() {
        releaseBuffers()
        effects = 0u
        modicon = 0u
        autotyper = false
        text = null
        pattern = null
    }
}
