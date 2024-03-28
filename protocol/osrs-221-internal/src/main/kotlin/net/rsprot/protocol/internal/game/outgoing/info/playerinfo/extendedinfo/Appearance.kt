package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.CachedExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.util.BodyType
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.util.PlayerBodyType

public class Appearance(capacity: Int) : CachedExtendedInfo(capacity) {
    public var name: String = ""
    public var combatLevel: UByte = 0u
    public var skillLevel: UShort = 0u
    public var hidden: Boolean = false
    public var male: Boolean = true
    public var textGender: UByte = MAX_UNSIGNED_BYTE
    public var skullIcon: UByte = MAX_UNSIGNED_BYTE
    public var overheadIcon: UByte = MAX_UNSIGNED_BYTE
    public var bodyType: BodyType = PlayerBodyType.DEFAULT
    public var colours: ByteArray = DEFAULT_COLOUR
    public var interfaceIdentKit: ShortArray = DEFAULT_INTERFACE_IDENT_KIT
    public var readyAnim: UShort = MAX_UNSIGNED_SHORT
    public var turnAnim: UShort = MAX_UNSIGNED_SHORT
    public var walkAnim: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimBack: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimLeft: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimRight: UShort = MAX_UNSIGNED_SHORT
    public var runAnim: UShort = MAX_UNSIGNED_SHORT
    // TODO: ObjTypeCustomisation

    override fun clear() {
        releaseBuffers()
        resetCache()
        name = ""
        combatLevel = 0u
        skillLevel = 0u
        hidden = false
        male = true
        textGender = MAX_UNSIGNED_BYTE
        skullIcon = MAX_UNSIGNED_BYTE
        overheadIcon = MAX_UNSIGNED_BYTE
        bodyType = PlayerBodyType.DEFAULT
        colours = DEFAULT_COLOUR
        interfaceIdentKit = DEFAULT_INTERFACE_IDENT_KIT
        readyAnim = MAX_UNSIGNED_SHORT
        turnAnim = MAX_UNSIGNED_SHORT
        walkAnim = MAX_UNSIGNED_SHORT
        walkAnimBack = MAX_UNSIGNED_SHORT
        walkAnimLeft = MAX_UNSIGNED_SHORT
        walkAnimRight = MAX_UNSIGNED_SHORT
        runAnim = MAX_UNSIGNED_SHORT
    }

    private companion object {
        private const val MAX_UNSIGNED_BYTE: UByte = 0xFFu
        private const val MAX_UNSIGNED_SHORT: UShort = 0xFFFFu
        private val DEFAULT_INTERFACE_IDENT_KIT: ShortArray = ShortArray(12)
        private val DEFAULT_COLOUR: ByteArray = ByteArray(5)
    }
}
