package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.shared.platform.PlatformType

// TODO: Optional bound checks (hits, etc)
@Suppress("MemberVisibilityCanBePrivate")
public class PlayerAvatarExtendedInfo(
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
    extendedInfoWriters: List<AvatarExtendedInfoWriter>,
    allocator: ByteBufAllocator,
    huffmanCodec: HuffmanCodec,
) {
    internal var flags: Int = 0
    private val blocks: PlayerAvatarExtendedInfoBlocks =
        PlayerAvatarExtendedInfoBlocks(
            extendedInfoWriters,
            allocator,
            huffmanCodec,
        )
    private val writers: Array<AvatarExtendedInfoWriter?> = buildPlatformWriterArray(extendedInfoWriters)

    public fun setMoveSpeed(value: Int) {
        blocks.moveSpeed.value = value
        flags = flags or MOVE_SPEED
    }

    public fun setTempMoveSpeed(value: Int) {
        blocks.temporaryMoveSpeed.value = value
        flags = flags or TEMP_MOVE_SPEED
    }

    public fun setSequence(
        id: Int,
        delay: Int,
    ) {
        blocks.sequence.id = id.toUShort()
        blocks.sequence.delay = delay.toUShort()
        flags = flags or SEQUENCE
    }

    public fun setFacePathingEntity(index: Int) {
        blocks.facePathingEntity.index = index
        flags = flags or FACE_PATHINGENTITY
    }

    public fun setFaceAngle(angle: Int) {
        blocks.faceAngle.angle = angle
        flags = flags or FACE_ANGLE
    }

    public fun setSay(text: String) {
        blocks.say.text = text
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
        blocks.chat.colour = colour.toUByte()
        blocks.chat.effects = effects.toUByte()
        blocks.chat.modicon = modicon.toUByte()
        blocks.chat.autotyper = autotyper
        blocks.chat.text = text
        blocks.chat.pattern = pattern
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
        blocks.exactMove.deltaX1 = deltaX1.toUByte()
        blocks.exactMove.deltaZ1 = deltaZ1.toUByte()
        blocks.exactMove.delay1 = delay1.toUShort()
        blocks.exactMove.deltaX2 = deltaX2.toUByte()
        blocks.exactMove.deltaZ2 = deltaZ2.toUByte()
        blocks.exactMove.delay2 = delay2.toUShort()
        blocks.exactMove.direction = direction.toUShort()
        flags = flags or EXACT_MOVE
    }

    public fun setSpotAnim(
        slot: Int,
        id: Int,
        delay: Int,
        height: Int,
    ) {
        blocks.spotAnims.set(slot, SpotAnim(id, delay, height))
        flags = flags or SPOTANIM
    }

    public fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        delay: Int = 0,
    ) {
        blocks.hit.hitMarkList +=
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
        blocks.hit.hitMarkList += HitMark(0x7FFEu, delay.toUShort())
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
        blocks.hit.hitMarkList +=
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
        blocks.hit.headBarList +=
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
            blocks.tinting.observerDependent[visibleToIndex] = tint
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = luminance.toUByte()
            tint.weight = opacity.toUByte()
            otherPlayerInfo.observerExtendedInfoFlags.addFlag(
                localIndex,
                TINTING,
            )
        } else {
            val tint = blocks.tinting.global
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
        blocks.appearance.name = name
        blocks.appearance.combatLevel = combatLevel.toUByte()
        blocks.appearance.skillLevel = skillLevel.toUShort()
        blocks.appearance.hidden = hidden
        blocks.appearance.male = male
        blocks.appearance.textGender = textGender.toUByte()
        blocks.appearance.skullIcon = skullIcon.toUByte()
        blocks.appearance.overheadIcon = overheadIcon.toUByte()
        flagAppearance()
    }

    public fun setName(name: String) {
        if (blocks.appearance.name == name) {
            return
        }
        blocks.appearance.name = name
        flagAppearance()
    }

    public fun setCombatLevel(combatLevel: Int) {
        val level = combatLevel.toUByte()
        if (blocks.appearance.combatLevel == level) {
            return
        }
        blocks.appearance.combatLevel = level
        flagAppearance()
    }

    public fun setSkillLevel(skillLevel: Int) {
        val level = skillLevel.toUShort()
        if (blocks.appearance.skillLevel == level) {
            return
        }
        blocks.appearance.skillLevel = level
        flagAppearance()
    }

    public fun setHidden(hidden: Boolean) {
        if (blocks.appearance.hidden == hidden) {
            return
        }
        blocks.appearance.hidden = hidden
        flagAppearance()
    }

    public fun setMale(isMale: Boolean) {
        if (blocks.appearance.male == isMale) {
            return
        }
        blocks.appearance.male = isMale
        flagAppearance()
    }

    public fun setTextGender(num: Int) {
        val textGender = num.toUByte()
        if (blocks.appearance.textGender == textGender) {
            return
        }
        blocks.appearance.textGender = textGender
        flagAppearance()
    }

    public fun setSkullIcon(icon: Int) {
        val skullIcon = icon.toUByte()
        if (blocks.appearance.skullIcon == skullIcon) {
            return
        }
        blocks.appearance.skullIcon = skullIcon
        flagAppearance()
    }

    public fun setOverheadIcon(icon: Int) {
        val overheadIcon = icon.toUByte()
        if (blocks.appearance.overheadIcon == overheadIcon) {
            return
        }
        blocks.appearance.overheadIcon = overheadIcon
        flagAppearance()
    }

    public fun transformToNpc(id: Int) {
        val npcId = id.toUShort()
        if (blocks.appearance.transformedNpcId == npcId) {
            return
        }
        blocks.appearance.transformedNpcId = npcId
        flagAppearance()
    }

    public fun setIdentKit(
        wearPos: Int,
        value: Int,
    ) {
        val valueAsShort = value.toShort()
        val cur = blocks.appearance.identKit[wearPos]
        if (cur == valueAsShort) {
            return
        }
        blocks.appearance.identKit[wearPos] = valueAsShort
        flagAppearance()
    }

    public fun setWornObj(
        wearpos: Int,
        id: Int,
        wearpos2: Int,
        wearpos3: Int,
    ) {
        val valueAsShort = id.toShort()
        val cur = blocks.appearance.wornObjs[wearpos]
        if (cur == valueAsShort) {
            return
        }
        blocks.appearance.wornObjs[wearpos] = valueAsShort
        val hiddenSlotsBitpacked = (wearpos2 and 0xF shl 4) or wearpos3 and 0xF
        blocks.appearance.hiddenWearPos[wearpos] = hiddenSlotsBitpacked.toByte()
        flagAppearance()
    }

    public fun setColour(
        slot: Int,
        value: Int,
    ) {
        val valueAsByte = value.toByte()
        val cur = blocks.appearance.colours[slot]
        if (cur == valueAsByte) {
            return
        }
        blocks.appearance.colours[slot] = valueAsByte
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
        blocks.appearance.readyAnim = readyAnim.toUShort()
        blocks.appearance.turnAnim = turnAnim.toUShort()
        blocks.appearance.walkAnim = walkAnim.toUShort()
        blocks.appearance.walkAnimBack = walkAnimBack.toUShort()
        blocks.appearance.walkAnimLeft = walkAnimLeft.toUShort()
        blocks.appearance.walkAnimRight = walkAnimRight.toUShort()
        blocks.appearance.runAnim = runAnim.toUShort()
        flagAppearance()
    }

    public fun nameExtras(
        beforeName: String,
        afterName: String,
        afterCombatLevel: String,
    ) {
        blocks.appearance.beforeName = beforeName
        blocks.appearance.afterName = afterName
        blocks.appearance.afterCombatLevel = afterCombatLevel
        flagAppearance()
    }

    public fun clearObjTypeCustomisation(wearpos: Int) {
        if (blocks.appearance.objTypeCustomisation[wearpos] == null) {
            return
        }
        blocks.appearance.objTypeCustomisation[wearpos] = null
        flagAppearance()
    }

    private fun allocObjCustomisation(wearpos: Int): ObjTypeCustomisation {
        var customisation = blocks.appearance.objTypeCustomisation[wearpos]
        if (customisation == null) {
            customisation = ObjTypeCustomisation()
            blocks.appearance.objTypeCustomisation[wearpos] = customisation
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
        blocks.appearance.changeCounter++
    }

    internal fun postUpdate() {
        clearTransientExtendedInformation()
        flags = 0
    }

    internal fun reset() {
        flags = 0
        blocks.appearance.clear()
        blocks.moveSpeed.clear()
        blocks.temporaryMoveSpeed.clear()
        blocks.sequence.clear()
        blocks.facePathingEntity.clear()
        blocks.faceAngle.clear()
        blocks.say.clear()
        blocks.chat.clear()
        blocks.exactMove.clear()
        blocks.spotAnims.clear()
        blocks.hit.clear()
        blocks.tinting.clear()
    }

    internal fun getLowToHighResChangeExtendedInfoFlags(observer: PlayerAvatarExtendedInfo): Int {
        var flag = 0
        if (this.flags and APPEARANCE == 0 &&
            checkOutOfDate(observer)
        ) {
            flag = flag or APPEARANCE
        }
        if (this.flags and MOVE_SPEED == 0 &&
            blocks.moveSpeed.value != MoveSpeed.DEFAULT_MOVESPEED
        ) {
            flag = flag or MOVE_SPEED
        }
        if (this.flags and FACE_PATHINGENTITY == 0 &&
            blocks.facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE
        ) {
            flag = flag or FACE_PATHINGENTITY
        }
        return flag
    }

    private fun checkOutOfDate(observer: PlayerAvatarExtendedInfo): Boolean {
        return observer.blocks.appearance.otherChangesCounter[localIndex] != blocks.appearance.changeCounter
    }

    internal fun precompute() {
        // Hits and tinting do not get precomputed
        if (flags and APPEARANCE != 0) {
            blocks.appearance.precompute()
        }
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.precompute()
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.precompute()
        }
        if (flags and FACE_ANGLE != 0) {
            blocks.faceAngle.precompute()
        }
        if (flags and SAY != 0) {
            blocks.say.precompute()
        }
        if (flags and CHAT != 0) {
            blocks.chat.precompute()
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.precompute()
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.precompute()
        }
    }

    internal fun pExtendedInfo(
        platformType: PlatformType,
        buffer: JagByteBuf,
        observerFlag: Int,
        observer: PlayerAvatarExtendedInfo,
    ) {
        // TODO: Figure out a way to cap this out
        if (buffer.writerIndex() >= 35_000) {
            buffer.p1(0)
            return
        }
        val writer =
            requireNotNull(writers[platformType.id]) {
                "Extended info writer missing for platform $platformType"
            }
        val flag = this.flags or observerFlag

        // If appearance is flagged, ensure we synchronize the changes counter
        if (flag and APPEARANCE != 0) {
            observer.blocks.appearance.otherChangesCounter[localIndex] = blocks.appearance.changeCounter
        }
        writer.pExtendedInfo(
            buffer,
            localIndex,
            observer.localIndex,
            flag,
            blocks,
        )
    }

    private fun clearTransientExtendedInformation() {
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.clear()
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.clear()
        }
        if (flags and FACE_ANGLE != 0) {
            blocks.faceAngle.clear()
        }
        if (flags and SAY != 0) {
            blocks.say.clear()
        }
        if (flags and CHAT != 0) {
            blocks.chat.clear()
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.clear()
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.clear()
        }
        if (flags and HITS != 0) {
            blocks.hit.clear()
        }
        if (flags and TINTING != 0) {
            blocks.tinting.clear()
        }
    }

    public companion object {
        // Observer-dependent flags, utilizing the lowest bits as we store observer flags in a byte array
        public const val APPEARANCE: Int = 0x1
        public const val MOVE_SPEED: Int = 0x2
        public const val FACE_PATHINGENTITY: Int = 0x4
        public const val TINTING: Int = 0x8

        // "Static" flags, the bit values here are irrelevant
        public const val FACE_ANGLE: Int = 0x10
        public const val SAY: Int = 0x20
        public const val HITS: Int = 0x40
        public const val SEQUENCE: Int = 0x80
        public const val CHAT: Int = 0x100
        public const val TEMP_MOVE_SPEED: Int = 0x200
        public const val EXACT_MOVE: Int = 0x400
        public const val SPOTANIM: Int = 0x800

        private fun buildPlatformWriterArray(
            extendedInfoWriters: List<AvatarExtendedInfoWriter>,
        ): Array<AvatarExtendedInfoWriter?> {
            val array = arrayOfNulls<AvatarExtendedInfoWriter>(PlatformType.COUNT)
            for (writer in extendedInfoWriters) {
                array[writer.platformType.id] = writer
            }
            return array
        }
    }
}
