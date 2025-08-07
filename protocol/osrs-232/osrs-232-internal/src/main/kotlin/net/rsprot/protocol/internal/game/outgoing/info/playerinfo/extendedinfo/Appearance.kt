package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.CachedExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder

/**
 * The appearance extended info block.
 * This is an unusually-large extended info block that is also the only extended info block
 * which gets cached client-side.
 * The library utilizes that caching through a counter which increments with each modification
 * done to the appearance. When an avatar goes from low resolution to high resolution,
 * a comparison is done against the cache, if the counters match, no extended info block is written.
 * If an avatar logs out, every observer will have their counter set back to -1.
 * @param encoders the array of client-specific encoders for appearance.
 */
public class Appearance(
    override val encoders: ClientTypeMap<PrecomputedExtendedInfoEncoder<Appearance>>,
) : CachedExtendedInfo<Appearance, PrecomputedExtendedInfoEncoder<Appearance>>() {
    /**
     * The name of this avatar.
     */
    public var name: String = ""

    /**
     * The combat level of this avatar.
     */
    public var combatLevel: UByte = 0u

    /**
     * The skill level of this avatar, shown on the right-click menu as "skill-number".
     * This is utilized within Burthorpe's games' room.
     */
    public var skillLevel: UShort = 0u

    /**
     * Whether this avatar is soft-hidden, meaning client will not render the model itself
     * for anyone except J-Mods. Clients such as RuneLite will ignore this property within
     * any plugins.
     */
    public var hidden: Boolean = false

    /**
     * The type of body the avatar is using.
     */
    public var bodyType: UByte = 0u

    /**
     * The type of pronoun to utilize within clientscripts.
     */
    public var pronoun: UByte = MAX_UNSIGNED_BYTE

    /**
     * The skull icon that appears over-head, mostly in PvP scenarios.
     */
    public var skullIcon: UByte = MAX_UNSIGNED_BYTE

    /**
     * The overhead icon that's utilized with prayers.
     */
    public var overheadIcon: UByte = MAX_UNSIGNED_BYTE

    /**
     * The id of the npc to which this avatar has transformed.
     */
    public var transformedNpcId: UShort = MAX_UNSIGNED_SHORT

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
    public var colours: ByteArray = ByteArray(COLOUR_COUNT)

    /**
     * The animation used when the avatar is standing still.
     */
    public var readyAnim: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is turning on-spot without movement.
     */
    public var turnAnim: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is walking forward.
     */
    public var walkAnim: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is walking backwards.
     */
    public var walkAnimBack: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is walking to the left.
     */
    public var walkAnimLeft: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is walking to the right.
     */
    public var walkAnimRight: UShort = MAX_UNSIGNED_SHORT

    /**
     * The animation used when the avatar is running.
     */
    public var runAnim: UShort = MAX_UNSIGNED_SHORT

    /**
     * Whether to force a model refresh client-side, removing the cached model of the player
     * even if the worn objects + base colour + gender have not changed.
     * This is important to flag when setting or removing an obj type customization.
     */
    public var forceModelRefresh: Boolean = false

    /**
     * The customisations applied to worn objs, indexed by the respective obj's primary wearpos.
     */
    public val objTypeCustomisation: Array<ObjTypeCustomisation?> = arrayOfNulls(12)

    /**
     * The string to render before an avatar's name in the right-click menu,
     * used within the Burthorpe games' room.
     */
    public var beforeName: String = ""

    /**
     * The string to render after an avatar's name in the right-click menu,
     * used within the Burthorpe games' room.
     */
    public var afterName: String = ""

    /**
     * The string to render after an avatar's combat level in the right-click menu,
     * used within the Burthorpe games' room.
     */
    public var afterCombatLevel: String = ""

    override fun clear() {
        releaseBuffers()
        name = ""
        combatLevel = 0u
        skillLevel = 0u
        hidden = false
        bodyType = 0u
        pronoun = MAX_UNSIGNED_BYTE
        skullIcon = MAX_UNSIGNED_BYTE
        overheadIcon = MAX_UNSIGNED_BYTE
        transformedNpcId = MAX_UNSIGNED_SHORT
        identKit.fill(-1)
        wornObjs.fill(-1)
        hiddenWearPos.fill(-1)
        colours.fill(0)
        forceModelRefresh = false
        objTypeCustomisation.fill(null)
        readyAnim = MAX_UNSIGNED_SHORT
        turnAnim = MAX_UNSIGNED_SHORT
        walkAnim = MAX_UNSIGNED_SHORT
        walkAnimBack = MAX_UNSIGNED_SHORT
        walkAnimLeft = MAX_UNSIGNED_SHORT
        walkAnimRight = MAX_UNSIGNED_SHORT
        runAnim = MAX_UNSIGNED_SHORT
    }

    public companion object {
        /**
         * The number of wearpos that the client will track.
         */
        private const val SLOT_COUNT: Int = 12

        /**
         * The number of colours that the client tracks.
         */
        private const val COLOUR_COUNT: Int = 5

        /**
         * A constant for max unsigned byte, frequently used as the "default, not initialized" value.
         */
        private const val MAX_UNSIGNED_BYTE: UByte = 0xFFu

        /**
         * A constant for max unsigned short, frequently used as the "default, not initialized" value.
         */
        private const val MAX_UNSIGNED_SHORT: UShort = 0xFFFFu

        private const val HAIR_IDENTKIT: Int = 0
        private const val BEARD_IDENTKIT: Int = 1
        private const val BODY_IDENTKIT: Int = 2
        private const val ARMS_IDENTKIT: Int = 3
        private const val GLOVES_IDENTKIT: Int = 4
        private const val LEGS_IDENTKIT: Int = 5
        private const val BOOTS_IDENTKIT: Int = 6

        /**
         * An array of wearpos -> ident kit slot, indexed by wearpos.
         */
        public val identKitSlotList: List<Int> =
            listOf(
                -1,
                -1,
                -1,
                -1,
                BODY_IDENTKIT,
                -1,
                ARMS_IDENTKIT,
                LEGS_IDENTKIT,
                HAIR_IDENTKIT,
                GLOVES_IDENTKIT,
                BOOTS_IDENTKIT,
                BEARD_IDENTKIT,
                -1,
                -1,
            )
    }
}
