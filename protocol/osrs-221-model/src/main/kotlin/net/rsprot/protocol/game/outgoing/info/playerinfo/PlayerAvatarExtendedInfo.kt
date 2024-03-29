package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.TintingList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim

// TODO: Optional bound checks (hits, etc)
@Suppress("MemberVisibilityCanBePrivate")
public class PlayerAvatarExtendedInfo(
    capacity: Int,
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
) {
    private val appearance: Appearance = Appearance(capacity)
    private val moveSpeed: MoveSpeed = MoveSpeed()
    private val temporaryMoveSpeed: TemporaryMoveSpeed = TemporaryMoveSpeed()
    private val sequence: Sequence = Sequence()
    private val facePathingEntity: FacePathingEntity = FacePathingEntity()
    private val faceAngle: FaceAngle = FaceAngle()
    private val say: Say = Say()
    private val chat: Chat = Chat()
    private val exactMove: ExactMove = ExactMove()
    private val spotAnims: SpotAnimList = SpotAnimList()
    private val hit: Hit = Hit()
    private val tinting: TintingList = TintingList()

    internal var flags: Int = 0

    public fun setMoveSpeed(value: Int) {
        moveSpeed.value = value
        flags = flags or MOVE_SPEED
    }

    public fun setTempMoveSpeed(value: Int) {
        temporaryMoveSpeed.value = value
        flags = flags or TEMP_MOVE_SPEED
    }

    public fun setSequence(
        id: Int,
        delay: Int,
    ) {
        sequence.id = id.toUShort()
        sequence.delay = delay.toUShort()
        flags = flags or SEQUENCE
    }

    public fun setFacePathingEntity(index: Int) {
        facePathingEntity.index = index
        flags = flags or FACE_PATHINGENTITY
    }

    public fun setFaceAngle(angle: Int) {
        faceAngle.angle = angle
        flags = flags or FACE_ANGLE
    }

    public fun setSay(text: String) {
        say.text = text
        flags = flags or SAY
    }

    public fun setChat(
        colour: Int,
        effects: Int,
        modicon: Int,
        autotyper: Boolean,
        text: String,
        pattern: ByteArray?,
    ) {
        val patternLength = if (colour in 13..20) colour - 12 else 0
        if (patternLength in 1..8) {
            requireNotNull(pattern) {
                "Pattern cannot be null if pattern length is defined."
            }
            require(pattern.size == patternLength) {
                "Pattern length does not match the size configured in the colour property."
            }
        }
        chat.colour = colour.toUByte()
        chat.effects = effects.toUByte()
        chat.modicon = modicon.toUByte()
        chat.autotyper = autotyper
        chat.text = text
        chat.pattern = pattern
        flags = flags or CHAT
    }

    public fun setExactMove(
        deltaX1: Int,
        deltaZ1: Int,
        delay1: Int,
        deltaX2: Int,
        deltaZ2: Int,
        delay2: Int,
        direction: Int,
    ) {
        exactMove.deltaX1 = deltaX1.toUByte()
        exactMove.deltaZ1 = deltaZ1.toUByte()
        exactMove.delay1 = delay1.toUShort()
        exactMove.deltaX2 = deltaX2.toUByte()
        exactMove.deltaZ2 = deltaZ2.toUByte()
        exactMove.delay2 = delay2.toUShort()
        exactMove.direction = direction.toUShort()
        flags = flags or EXACT_MOVE
    }

    public fun setSpotAnim(
        slot: Int,
        id: Int,
        delay: Int,
        height: Int,
    ) {
        spotAnims.set(slot, SpotAnim(id, delay, height))
        flags = flags or SPOTANIM
    }

    public fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        delay: Int = 0,
    ) {
        hit.hitMarkList +=
            HitMark(
                sourceIndex,
                selfType.toUShort(),
                otherType.toUShort(),
                value.toUShort(),
                delay.toUShort(),
            )
        flags = flags or HITS
    }

    public fun removeHitMark(delay: Int = 0) {
        hit.hitMarkList += HitMark(0x7FFEu, delay.toUShort())
        flags = flags or HITS
    }

    public fun addSoakedHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        selfSoakType: Int,
        otherSoakType: Int = selfSoakType,
        soakValue: Int,
        delay: Int = 0,
    ) {
        hit.hitMarkList +=
            HitMark(
                sourceIndex,
                selfType.toUShort(),
                otherType.toUShort(),
                value.toUShort(),
                selfSoakType.toUShort(),
                otherSoakType.toUShort(),
                soakValue.toUShort(),
                delay.toUShort(),
            )
        flags = flags or HITS
    }

    public fun addHeadBar(
        id: Int,
        startFill: Int,
        endFill: Int = startFill,
        startTime: Int = 0,
        endTime: Int = 0,
    ) {
        hit.headBarList +=
            HeadBar(
                id.toUShort(),
                startFill.toUByte(),
                endFill.toUByte(),
                startTime.toUShort(),
                endTime.toUShort(),
            )
        flags = flags or HITS
    }

    public fun removeHeadBar(id: Int) {
        addHeadBar(
            id,
            startFill = 0,
            endTime = HeadBar.REMOVED.toInt(),
        )
    }

    public fun tinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        luminance: Int,
        opacity: Int,
        visibleToIndex: Int = -1,
    ) {
        if (visibleToIndex != -1) {
            val otherPlayerInfo = protocol.getPlayerInfo(visibleToIndex)
            requireNotNull(otherPlayerInfo) {
                "Player at index $visibleToIndex does not exist."
            }
            val tint = Tinting()
            this.tinting.observerDependent[visibleToIndex] = tint
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = luminance.toUByte()
            tint.weight = opacity.toUByte()
            otherPlayerInfo.observerExtendedInfoFlags.addFlag(localIndex, TINTING)
        } else {
            val tint = this.tinting.global
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = luminance.toUByte()
            tint.weight = opacity.toUByte()
            flags = flags or TINTING
        }
    }

    public fun initializeAppearance(
        name: String,
        combatLevel: Int,
        skillLevel: Int,
        hidden: Boolean,
        male: Boolean,
        textGender: Int,
        skullIcon: Int,
        overheadIcon: Int,
    ) {
        appearance.name = name
        appearance.combatLevel = combatLevel.toUByte()
        appearance.skillLevel = skillLevel.toUShort()
        appearance.hidden = hidden
        appearance.male = male
        appearance.textGender = textGender.toUByte()
        appearance.skullIcon = skullIcon.toUByte()
        appearance.overheadIcon = overheadIcon.toUByte()
        flagAppearance()
    }

    public fun setName(name: String) {
        if (appearance.name == name) {
            return
        }
        appearance.name = name
        flagAppearance()
    }

    public fun setCombatLevel(combatLevel: Int) {
        val level = combatLevel.toUByte()
        if (appearance.combatLevel == level) {
            return
        }
        appearance.combatLevel = level
        flagAppearance()
    }

    public fun setSkillLevel(skillLevel: Int) {
        val level = skillLevel.toUShort()
        if (appearance.skillLevel == level) {
            return
        }
        appearance.skillLevel = level
        flagAppearance()
    }

    public fun setHidden(hidden: Boolean) {
        if (appearance.hidden == hidden) {
            return
        }
        appearance.hidden = hidden
        flagAppearance()
    }

    public fun setMale(isMale: Boolean) {
        if (appearance.male == isMale) {
            return
        }
        appearance.male = isMale
        flagAppearance()
    }

    public fun setTextGender(num: Int) {
        val textGender = num.toUByte()
        if (appearance.textGender == textGender) {
            return
        }
        appearance.textGender = textGender
        flagAppearance()
    }

    public fun setSkullIcon(icon: Int) {
        val skullIcon = icon.toUByte()
        if (appearance.skullIcon == skullIcon) {
            return
        }
        appearance.skullIcon = skullIcon
        flagAppearance()
    }

    public fun setOverheadIcon(icon: Int) {
        val overheadIcon = icon.toUByte()
        if (appearance.overheadIcon == overheadIcon) {
            return
        }
        appearance.overheadIcon = overheadIcon
        flagAppearance()
    }

    public fun transformToNpc(id: Int) {
        val npcId = id.toUShort()
        if (appearance.transformedNpcId == npcId) {
            return
        }
        appearance.transformedNpcId = npcId
        flagAppearance()
    }

    public fun setIdentKit(
        wearPos: Int,
        value: Int,
    ) {
        val valueAsShort = value.toShort()
        val cur = appearance.identKit[wearPos]
        if (cur == valueAsShort) {
            return
        }
        appearance.identKit[wearPos] = valueAsShort
        flagAppearance()
    }

    public fun setWornObj(
        wearpos: Int,
        id: Int,
        wearpos2: Int,
        wearpos3: Int,
    ) {
        val valueAsShort = id.toShort()
        val cur = appearance.wornObjs[wearpos]
        if (cur == valueAsShort) {
            return
        }
        appearance.wornObjs[wearpos] = valueAsShort
        val hiddenSlotsBitpacked = (wearpos2 and 0xF shl 4) or wearpos3 and 0xF
        appearance.hiddenWearPos[wearpos] = hiddenSlotsBitpacked.toByte()
        flagAppearance()
    }

    public fun setColour(
        slot: Int,
        value: Int,
    ) {
        val valueAsByte = value.toByte()
        val cur = appearance.colours[slot]
        if (cur == valueAsByte) {
            return
        }
        appearance.colours[slot] = valueAsByte
        flagAppearance()
    }

    public fun setBaseAnimationSet(
        readyAnim: Int,
        turnAnim: Int,
        walkAnim: Int,
        walkAnimBack: Int,
        walkAnimLeft: Int,
        walkAnimRight: Int,
        runAnim: Int,
    ) {
        appearance.readyAnim = readyAnim.toUShort()
        appearance.turnAnim = turnAnim.toUShort()
        appearance.walkAnim = walkAnim.toUShort()
        appearance.walkAnimBack = walkAnimBack.toUShort()
        appearance.walkAnimLeft = walkAnimLeft.toUShort()
        appearance.walkAnimRight = walkAnimRight.toUShort()
        appearance.runAnim = runAnim.toUShort()
        flagAppearance()
    }

    public fun nameExtras(
        beforeName: String,
        afterName: String,
        afterCombatLevel: String,
    ) {
        appearance.beforeName = beforeName
        appearance.afterName = afterName
        appearance.afterCombatLevel = afterCombatLevel
        flagAppearance()
    }

    public fun clearObjTypeCustomisation(wearpos: Int) {
        if (appearance.objTypeCustomisation[wearpos] == null) {
            return
        }
        appearance.objTypeCustomisation[wearpos] = null
        flagAppearance()
    }

    private fun allocObjCustomisation(wearpos: Int): ObjTypeCustomisation {
        var customisation = appearance.objTypeCustomisation[wearpos]
        if (customisation == null) {
            customisation = ObjTypeCustomisation()
            appearance.objTypeCustomisation[wearpos] = customisation
        }
        return customisation
    }

    public fun objRecol1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        val customisation = allocObjCustomisation(wearpos)
        customisation.recolIndices = ((customisation.recolIndices.toInt() and 0xF0) or (index and 0xF)).toUByte()
        customisation.recol1 = value.toUShort()
        flagAppearance()
    }

    public fun objRecol2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        val customisation = allocObjCustomisation(wearpos)
        customisation.recolIndices = ((customisation.recolIndices.toInt() and 0xF) or ((index and 0xF) shl 4)).toUByte()
        customisation.recol2 = value.toUShort()
        flagAppearance()
    }

    public fun objRetex1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        val customisation = allocObjCustomisation(wearpos)
        customisation.retexIndices = ((customisation.retexIndices.toInt() and 0xF0) or (index and 0xF)).toUByte()
        customisation.retex1 = value.toUShort()
        flagAppearance()
    }

    public fun objRetex2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        val customisation = allocObjCustomisation(wearpos)
        customisation.retexIndices = ((customisation.retexIndices.toInt() and 0xF) or ((index and 0xF) shl 4)).toUByte()
        customisation.retex2 = value.toUShort()
        flagAppearance()
    }

    private fun flagAppearance() {
        flags = flags or APPEARANCE
        appearance.changeCounter++
    }

    internal fun reset() {
        clearTransientExtendedInformation()
        flags = 0
    }

    internal fun getLowToHighResChangeExtendedInfoFlags(observer: PlayerAvatarExtendedInfo): Int {
        var flag = 0
        if (checkOutOfDate(observer)) {
            flag = flag or APPEARANCE
        }
        if (moveSpeed.value != MoveSpeed.DEFAULT_MOVESPEED) {
            flag = flag or MOVE_SPEED
        }
        if (facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE) {
            flag = flag or FACE_PATHINGENTITY
        }
        return flag
    }

    private fun checkOutOfDate(observer: PlayerAvatarExtendedInfo): Boolean {
        val isOutOfDate = observer.appearance.otherChangesCounter[localIndex] != appearance.changeCounter
        if (isOutOfDate) {
            observer.appearance.otherChangesCounter[localIndex] = appearance.changeCounter
        }
        return isOutOfDate
    }

    private fun clearTransientExtendedInformation() {
        if (flags and TEMP_MOVE_SPEED != 0) {
            temporaryMoveSpeed.clear()
        }
        if (flags and SEQUENCE != 0) {
            sequence.clear()
        }
        if (flags and FACE_ANGLE != 0) {
            faceAngle.clear()
        }
        if (flags and SAY != 0) {
            say.clear()
        }
        if (flags and CHAT != 0) {
            chat.clear()
        }
        if (flags and EXACT_MOVE != 0) {
            exactMove.clear()
        }
        if (flags and SPOTANIM != 0) {
            spotAnims.clear()
        }
        if (flags and HITS != 0) {
            hit.clear()
        }
        if (flags and TINTING != 0) {
            tinting.clear()
        }
    }

    public companion object {
        internal const val EXTENDED_SHORT = 0x1
        internal const val FACE_ANGLE = 0x2
        internal const val APPEARANCE = 0x4
        internal const val SAY = 0x8
        internal const val CHAT_OLD = 0x10
        internal const val HITS = 0x20
        internal const val FACE_PATHINGENTITY = 0x40
        internal const val SEQUENCE = 0x80
        internal const val MOVE_SPEED = 0x200
        internal const val CHAT = 0x400
        internal const val EXTENDED_MEDIUM = 0x800
        internal const val TEMP_MOVE_SPEED = 0x1000
        internal const val TINTING = 0x2000
        internal const val EXACT_MOVE = 0x4000
        internal const val SPOTANIM = 0x10000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        internal const val NAME_EXTRAS = 0x100
    }
}
