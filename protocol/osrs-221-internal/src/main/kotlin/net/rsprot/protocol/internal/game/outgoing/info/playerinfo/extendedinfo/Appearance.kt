package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.CachedExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class Appearance(
    capacity: Int,
    encoders: Array<PrecomputedExtendedInfoEncoder<Appearance>?> = arrayOfNulls(PlatformType.COUNT),
) : CachedExtendedInfo<Appearance, PrecomputedExtendedInfoEncoder<Appearance>>(
        capacity,
        encoders,
    ) {
    public var name: String = ""
    public var combatLevel: UByte = 0u
    public var skillLevel: UShort = 0u
    public var hidden: Boolean = false
    public var male: Boolean = true
    public var textGender: UByte = MAX_UNSIGNED_BYTE
    public var skullIcon: UByte = MAX_UNSIGNED_BYTE
    public var overheadIcon: UByte = MAX_UNSIGNED_BYTE
    public var transformedNpcId: UShort = MAX_UNSIGNED_SHORT
    public val identKit: ShortArray = ShortArray(SLOT_COUNT) { -1 }
    public val wornObjs: ShortArray = ShortArray(SLOT_COUNT) { -1 }
    public val hiddenWearPos: ByteArray = ByteArray(SLOT_COUNT) { -1 }
    public var colours: ByteArray = ByteArray(COLOUR_COUNT)
    public var readyAnim: UShort = MAX_UNSIGNED_SHORT
    public var turnAnim: UShort = MAX_UNSIGNED_SHORT
    public var walkAnim: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimBack: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimLeft: UShort = MAX_UNSIGNED_SHORT
    public var walkAnimRight: UShort = MAX_UNSIGNED_SHORT
    public var runAnim: UShort = MAX_UNSIGNED_SHORT
    public val objTypeCustomisation: Array<ObjTypeCustomisation?> = arrayOfNulls(12)
    public var beforeName: String = ""
    public var afterName: String = ""
    public var afterCombatLevel: String = ""

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
        transformedNpcId = MAX_UNSIGNED_SHORT
        identKit.fill(-1)
        wornObjs.fill(-1)
        hiddenWearPos.fill(-1)
        colours.fill(0)
        objTypeCustomisation.fill(null)
        readyAnim = MAX_UNSIGNED_SHORT
        turnAnim = MAX_UNSIGNED_SHORT
        walkAnim = MAX_UNSIGNED_SHORT
        walkAnimBack = MAX_UNSIGNED_SHORT
        walkAnimLeft = MAX_UNSIGNED_SHORT
        walkAnimRight = MAX_UNSIGNED_SHORT
        runAnim = MAX_UNSIGNED_SHORT
    }

    private companion object {
        private const val SLOT_COUNT: Int = 12
        private const val COLOUR_COUNT: Int = 5
        private const val MAX_UNSIGNED_BYTE: UByte = 0xFFu
        private const val MAX_UNSIGNED_SHORT: UShort = 0xFFFFu
    }
}
