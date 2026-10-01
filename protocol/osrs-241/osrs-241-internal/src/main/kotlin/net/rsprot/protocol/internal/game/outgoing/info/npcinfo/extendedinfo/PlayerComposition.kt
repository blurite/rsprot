package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance.Companion.COLOUR_COUNT
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance.Companion.SLOT_COUNT

public class PlayerComposition {
    /**
     * The type of body the avatar is using.
     */
    public var bodyType: UByte = 0u

    /**
     * An array of ident kit ids, indexed by the respective wearpos.
     */
    public val identKit: ShortArray = ShortArray(7) { -1 }

    /**
     * The worn obj ids, indexed by the respective wearpos.
     */
    public val wornObjs: ShortArray = ShortArray(SLOT_COUNT) { -1 }

    /**
     * The secondary and tertiary wearpos that the primary wearpos
     * ends up hiding. The secondary and tertiary values are bitpacked
     * into a single byte. We track this separately, so we can always
     * get the full idea of what the avatar is built up out of.
     */
    public val hiddenWearPos: ByteArray = ByteArray(SLOT_COUNT) { -1 }

    /**
     * The colours the avatar's model is made up of.
     */
    public var colours: ByteArray? = null

    public fun getOrCreateColoursArray(): ByteArray {
        val colours = this.colours
        if (colours != null) {
            return colours
        }
        val newColours = ByteArray(COLOUR_COUNT)
        this.colours = newColours
        return newColours
    }
}
